package com.sewagealert.auth.service.impl;

import com.sewagealert.auth.client.UserServiceClient;
import com.sewagealert.auth.dto.AuthResponse;
import com.sewagealert.auth.dto.LoginRequest;
import com.sewagealert.auth.dto.RegisterRequest;
import com.sewagealert.auth.exception.EmailAlreadyExistsException;
import com.sewagealert.auth.exception.EmailNotVerifiedException;
import com.sewagealert.auth.exception.InvalidCredentialsException;
import com.sewagealert.auth.model.Role;
import com.sewagealert.auth.model.User;
import com.sewagealert.auth.producer.NotificationEventProducer;
import com.sewagealert.auth.repository.UserRepository;
import com.sewagealert.auth.security.JwtTokenProvider;
import com.sewagealert.auth.service.EmailVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImplTest: Registration (creates unverified account, publishes USER_REGISTERED,
 * no JWT), login gating (unverified rejected), verification and resend delegation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock private UserServiceClient userServiceClient;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private EmailVerificationService emailVerificationService;
    @Mock private NotificationEventProducer notificationEventProducer;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userServiceClient, userRepository, passwordEncoder,
                jwtTokenProvider, emailVerificationService, notificationEventProducer);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(passwordEncoder.matches(eq("correct-password"), any())).thenReturn(true);
        when(emailVerificationService.createVerificationToken(any())).thenReturn("raw-verification-token");
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Siddhartha");
        request.setEmail("customer@example.com");
        request.setPassword("correct-password");
        request.setPhone(9876543210L);
        return request;
    }

    private User savedUser() {
        User user = new User("Siddhartha", "customer@example.com", "encoded-password", 9876543210L, Role.CITIZEN);
        user.setId(1L);
        user.setEmailVerified(false);
        return user;
    }

    // --- registration --------------------------------------------------------

    @Test
    void registerCreatesUnverifiedUserPublishesEventAndIssuesNoJwt() {
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthResponse response = service.register(registerRequest());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().isEmailVerified()).isFalse();

        verify(emailVerificationService).createVerificationToken(1L);
        verify(notificationEventProducer).publishUserRegistered(eq(1L), eq("Siddhartha"),
                eq("customer@example.com"), eq("raw-verification-token"));

        // No JWT — the account must be verified before login
        assertThat(response.getToken()).isNull();
        assertThat(response.getRole()).isEqualTo(Role.CITIZEN);
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(registerRequest()))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
        verify(notificationEventProducer, never()).publishUserRegistered(any(), any(), any(), any());
    }

    // --- login ---------------------------------------------------------------

    @Test
    void verifiedUserCanLoginAndReceivesJwt() {
        User user = savedUser();
        user.setEmailVerified(true);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(1L, "customer@example.com", Role.CITIZEN)).thenReturn("jwt-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("customer@example.com");
        request.setPassword("correct-password");

        AuthResponse response = service.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void unverifiedUserCannotLogin() {
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(savedUser()));

        LoginRequest request = new LoginRequest();
        request.setEmail("customer@example.com");
        request.setPassword("correct-password");

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(EmailNotVerifiedException.class);
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    @Test
    void wrongPasswordRejectedBeforeVerificationCheck() {
        User user = savedUser();
        user.setEmailVerified(true);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("customer@example.com");
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(jwtTokenProvider, never()).generateToken(any(), any(), any());
    }

    // --- verification --------------------------------------------------------

    @Test
    void verifyEmailPublishesWelcomeEventForVerifiedUser() {
        User user = savedUser();
        user.setEmailVerified(true);
        when(emailVerificationService.verifyEmail("raw-token")).thenReturn(user);

        service.verifyEmail("raw-token");

        verify(notificationEventProducer).publishEmailVerified(1L, "Siddhartha", "customer@example.com");
    }

    // --- resend --------------------------------------------------------------

    @Test
    void resendVerificationPublishesRequestEventWhenTokenIssued() {
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(savedUser()));
        when(emailVerificationService.resendVerification(1L)).thenReturn("fresh-token");

        service.resendVerification("customer@example.com");

        verify(notificationEventProducer).publishVerificationRequested(1L, "Siddhartha", "customer@example.com", "fresh-token");
    }

    @Test
    void resendVerificationPublishesNothingWhenThrottledOrVerified() {
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(savedUser()));
        when(emailVerificationService.resendVerification(1L)).thenReturn(null);

        service.resendVerification("customer@example.com");

        verify(notificationEventProducer, never()).publishVerificationRequested(any(), any(), any(), any());
    }

    @Test
    void resendVerificationForUnknownEmailDoesNothing() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        service.resendVerification("ghost@example.com");

        verify(emailVerificationService, never()).resendVerification(any());
        verify(notificationEventProducer, never()).publishVerificationRequested(any(), any(), any(), any());
    }
}
