package com.sewagealert.auth.service.impl;

import com.sewagealert.auth.config.VerificationProperties;
import com.sewagealert.auth.exception.InvalidVerificationTokenException;
import com.sewagealert.auth.model.EmailVerificationToken;
import com.sewagealert.auth.model.User;
import com.sewagealert.auth.repository.EmailVerificationTokenRepository;
import com.sewagealert.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EmailVerificationServiceImplTest: Token lifecycle — generation (hashed, expiring),
 * verification (valid / invalid / expired / used), and resend throttling.
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;

    private EmailVerificationServiceImpl service;
    private VerificationProperties verificationProperties;

    @BeforeEach
    void setUp() {
        verificationProperties = new VerificationProperties();
        verificationProperties.setTokenTtlMinutes(30);
        service = new EmailVerificationServiceImpl(userRepository, tokenRepository, verificationProperties);
    }

    private User unverifiedUser() {
        User user = new User("Siddhartha", "customer@example.com", "encoded", null, null);
        user.setId(7L);
        user.setEmailVerified(false);
        return user;
    }

    private EmailVerificationToken validToken(long userId) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setId(1L);
        token.setUserId(userId);
        token.setTokenHash("some-hash");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        return token;
    }

    // --- token creation ------------------------------------------------------

    @Test
    void createVerificationTokenStoresHashedExpiringTokenAndInvalidatesOldOnes() {
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String rawToken = service.createVerificationToken(7L);

        assertThat(rawToken).isNotBlank();
        assertThat(rawToken).hasSizeGreaterThan(20);
        // The token is random 256-bit data — it can never be the numeric user id
        assertThat(rawToken).isNotEqualTo("7");

        verify(tokenRepository).invalidateByUser(7L);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken stored = captor.getValue();

        // Only the SHA-256 hash is persisted — never the raw token
        assertThat(stored.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(stored.getTokenHash()).hasSize(64);
        assertThat(stored.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(stored.isUsed()).isFalse();
    }

    // --- verification --------------------------------------------------------

    @Test
    void verifyEmailWithValidTokenVerifiesUserAndMarksTokenUsed() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(7L);
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        User verified = service.verifyEmail("raw-token");

        assertThat(verified.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyEmailWithUnknownTokenRejected() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmail("unknown-token"))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }

    @Test
    void verifyEmailWithExpiredTokenRejected() {
        EmailVerificationToken token = validToken(7L);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmail("expired-token"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        assertThat(token.isUsed()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailWithUsedTokenRejected() {
        EmailVerificationToken token = validToken(7L);
        token.setUsed(true);
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmail("used-token"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailWithBlankTokenRejected() {
        assertThatThrownBy(() -> service.verifyEmail("  "))
                .isInstanceOf(InvalidVerificationTokenException.class);
        verify(tokenRepository, never()).findByTokenHash(any());
    }

    // --- resend --------------------------------------------------------------

    @Test
    void resendVerificationIssuesFreshTokenForUnverifiedUser() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(unverifiedUser()));
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String first = service.resendVerification(7L);

        assertThat(first).isNotBlank();
        verify(tokenRepository).invalidateByUser(7L);
    }

    @Test
    void resendVerificationIsRateLimitedWithinWindow() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(unverifiedUser()));
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String first = service.resendVerification(7L);
        String second = service.resendVerification(7L); // immediate retry

        assertThat(first).isNotBlank();
        assertThat(second).isNull();
    }

    @Test
    void resendVerificationReturnsNullForAlreadyVerifiedUser() {
        User verified = unverifiedUser();
        verified.setEmailVerified(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(verified));

        assertThat(service.resendVerification(7L)).isNull();
        verify(tokenRepository, never()).save(any());
    }
}
