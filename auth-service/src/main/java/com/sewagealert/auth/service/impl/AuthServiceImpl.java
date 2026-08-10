package com.sewagealert.auth.service.impl;

import com.sewagealert.auth.client.UserServiceClient;
import com.sewagealert.auth.dto.AuthResponse;
import com.sewagealert.auth.dto.CreateUserProfileRequest;
import com.sewagealert.auth.dto.FieldOfficerResponse;
import com.sewagealert.auth.dto.LoginRequest;
import com.sewagealert.auth.dto.RegisterRequest;
import com.sewagealert.auth.dto.UserRoleResponse;
import com.sewagealert.auth.exception.EmailAlreadyExistsException;
import com.sewagealert.auth.exception.InvalidCredentialsException;
import com.sewagealert.auth.exception.UserNotFoundException;
import com.sewagealert.auth.model.Role;
import com.sewagealert.auth.model.User;
import com.sewagealert.auth.repository.UserRepository;
import com.sewagealert.auth.security.JwtTokenProvider;
import com.sewagealert.auth.service.AuthService;
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

        user = userRepository.save(user);

        CreateUserProfileRequest profileRequest =
                new CreateUserProfileRequest();

        profileRequest.setAuthUserId(user.getId());
        profileRequest.setName(user.getName());
        profileRequest.setPhone(user.getPhone());

        try {
            userServiceClient.createProfile(profileRequest);
        } catch (Exception ex) {
            log.error("Failed to create user profile", ex);

            // We'll decide how to handle this:
            // - rollback
            // - retry
            // - compensation
            // - event-based approach
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        log.info("New user registered: {} with role {}", user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
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