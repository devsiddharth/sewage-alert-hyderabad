package com.sewagealert.auth.service.impl;

import com.sewagealert.auth.client.UserServiceClient;
import com.sewagealert.auth.dto.AuthResponse;
import com.sewagealert.auth.dto.CreateUserProfileRequest;
import com.sewagealert.auth.dto.FieldOfficerResponse;
import com.sewagealert.auth.dto.LoginRequest;
import com.sewagealert.auth.dto.RegisterRequest;
import com.sewagealert.auth.dto.UserRoleResponse;
import com.sewagealert.auth.exception.EmailAlreadyExistsException;
import com.sewagealert.auth.exception.EmailNotVerifiedException;
import com.sewagealert.auth.exception.InvalidCredentialsException;
import com.sewagealert.auth.exception.UserNotFoundException;
import com.sewagealert.auth.model.Role;
import com.sewagealert.auth.model.User;
import com.sewagealert.auth.producer.NotificationEventProducer;
import com.sewagealert.auth.repository.UserRepository;
import com.sewagealert.auth.security.JwtTokenProvider;
import com.sewagealert.auth.service.AuthService;
import com.sewagealert.auth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;
    private final NotificationEventProducer notificationEventProducer;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        Role role = Role.CITIZEN;

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                role
        );
        // The constructor sets emailVerified=false — new accounts stay unverified until
        // they confirm the email, and login stays blocked until then.
        user = userRepository.save(user);

        CreateUserProfileRequest profileRequest =
                new CreateUserProfileRequest();

        profileRequest.setAuthUserId(user.getId());
        profileRequest.setName(user.getName());
        profileRequest.setPhone(user.getPhone());

        try {
            userServiceClient.createProfile(profileRequest);
        } catch (Exception ex) {
            log.error("Failed to create user profile for userId={}", user.getId(), ex);
            // Profile creation is best-effort (existing behaviour) — the account remains usable.
        }

        // Generate the 6-digit verification code and hand it to the Notification Service via
        // RabbitMQ — the Auth Service never talks to the email provider directly. Verification
        // is OTP-only: the code is emailed and typed back into the registration page.
        String otp = emailVerificationService.createVerification(user.getId());
        notificationEventProducer.publishUserRegistered(
                user.getId(), user.getName(), user.getEmail(), otp);

        log.info("User registration event published for userId={}, role={}",
                user.getId(), user.getRole());

        // No JWT is issued — the account is not active until the email is verified.
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email address before logging in.");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public void verifyEmailWithCode(String email, String code) {
        User user = emailVerificationService.verifyEmailWithCode(email, code);
        // Fire the welcome-email event so the Notification Service can send it (optional channel)
        notificationEventProducer.publishEmailVerified(user.getId(), user.getName(), user.getEmail());
        log.info("Email verified via code and EMAIL_VERIFIED event published for userId={}", user.getId());
    }

    @Override
    public void resendVerification(String email) {
        // Account enumeration protection: the response is identical whether the email is
        // unknown, already verified, or successfully re-issued.
        userRepository.findByEmail(email).ifPresent(user -> {
            String otp = emailVerificationService.resendVerification(user.getId());
            if (otp != null) {
                notificationEventProducer.publishVerificationRequested(
                        user.getId(), user.getName(), user.getEmail(), otp);
            }
        });
    }

    @Override
    public AuthResponse getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    // getFieldOfficers: All FIELD_OFFICER users, projected to a safe DTO (id/name/email only)
    public List<FieldOfficerResponse> getFieldOfficers() {
        return userRepository.findByRole(Role.FIELD_OFFICER).stream()
                .map(FieldOfficerResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // getUserRoleInfo: Identity + role lookup used by other services for server-side
    // authorization — the user must exist, otherwise the caller gets a 404.
    public UserRoleResponse getUserRoleInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return UserRoleResponse.fromEntity(user);
    }
}