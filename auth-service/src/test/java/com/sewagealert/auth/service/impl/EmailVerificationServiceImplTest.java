package com.sewagealert.auth.service.impl;

import com.sewagealert.auth.config.VerificationProperties;
import com.sewagealert.auth.exception.InvalidVerificationTokenException;
import com.sewagealert.auth.model.EmailVerificationToken;
import com.sewagealert.auth.model.User;
import com.sewagealert.auth.repository.EmailVerificationTokenRepository;
import com.sewagealert.auth.repository.UserRepository;
import com.sewagealert.auth.util.VerificationTokenGenerator;
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
 * EmailVerificationServiceImplTest: Verification lifecycle — issuance (hashed 6-digit code,
 * expiring), inline code verification (valid / wrong / unknown email / lockout), and resend
 * throttling. OTP-only — there is no emailed link/token.
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
        token.setOtpHash(VerificationTokenGenerator.hash("123456"));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        return token;
    }

    // --- credential creation -------------------------------------------------

    @Test
    void createVerificationStoresHashedOtpAndInvalidatesOldOnes() {
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String otp = service.createVerification(7L);

        // The code is 6 digits (100000–999999, so no leading zero)
        assertThat(otp).matches("\\d{6}");

        verify(tokenRepository).invalidateByUser(7L);

        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        EmailVerificationToken stored = captor.getValue();

        // Only the SHA-256 hash is persisted — never the raw code
        assertThat(stored.getOtpHash()).isNotEqualTo(otp);
        assertThat(stored.getOtpHash()).hasSize(64);
        assertThat(stored.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(stored.isUsed()).isFalse();
    }

    // --- inline code verification --------------------------------------------

    @Test
    void verifyEmailWithValidCodeVerifiesUserAndMarksTokenUsed() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(7L);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.of(token));

        User verified = service.verifyEmailWithCode("customer@example.com", "123456");

        assertThat(verified.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyEmailWithWrongCodeRejected() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(7L);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmailWithCode("customer@example.com", "654321"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(token.isUsed()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailWithExpiredCodeRejected() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(7L);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verifyEmailWithCode("customer@example.com", "123456"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        assertThat(token.isUsed()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailWithCodeForUnknownEmailRejected() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmailWithCode("ghost@example.com", "123456"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        verify(tokenRepository, never()).findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(any());
    }

    @Test
    void verifyEmailWithCodeForAlreadyVerifiedUserDoesNotLeakState() {
        // A verified account has no active token left, so the endpoint answers with the
        // same generic error as an unknown email — no enumeration side-channel on a
        // public endpoint (a correct code is required to reach the account at all).
        User user = unverifiedUser();
        user.setEmailVerified(true);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.verifyEmailWithCode("customer@example.com", "123456"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmailWithCodeLocksOutAfterRepeatedFailures() {
        User user = unverifiedUser();
        EmailVerificationToken token = validToken(7L);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(7L))
                .thenReturn(Optional.of(token));

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.verifyEmailWithCode("customer@example.com", "999999"))
                    .isInstanceOf(InvalidVerificationTokenException.class);
        }

        // The 6th attempt is rejected while locked out — the code is never even checked
        assertThatThrownBy(() -> service.verifyEmailWithCode("customer@example.com", "123456"))
                .isInstanceOf(InvalidVerificationTokenException.class);
        verify(userRepository, never()).save(user);
    }

    // --- resend --------------------------------------------------------------

    @Test
    void resendVerificationIssuesFreshCodeForUnverifiedUser() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(unverifiedUser()));
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String otp = service.resendVerification(7L);

        assertThat(otp).isNotNull();
        assertThat(otp).matches("\\d{6}");
        verify(tokenRepository).invalidateByUser(7L);
    }

    @Test
    void resendVerificationIsRateLimitedWithinWindow() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(unverifiedUser()));
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String first = service.resendVerification(7L);
        String second = service.resendVerification(7L); // immediate retry

        assertThat(first).isNotNull();
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
