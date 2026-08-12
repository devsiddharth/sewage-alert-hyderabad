package com.sewagealert.auth.service.impl;

import com.sewagealert.auth.config.VerificationProperties;
import com.sewagealert.auth.exception.InvalidVerificationTokenException;
import com.sewagealert.auth.model.EmailVerificationToken;
import com.sewagealert.auth.model.User;
import com.sewagealert.auth.repository.EmailVerificationTokenRepository;
import com.sewagealert.auth.repository.UserRepository;
import com.sewagealert.auth.service.EmailVerificationService;
import com.sewagealert.auth.util.VerificationTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

// EmailVerificationServiceImpl: Owns the verification lifecycle — issuance of the 6-digit
// code, validation (with brute-force throttling), invalidation and resend throttling.
// OTP-only: no emailed link/token exists. The raw code is never logged.
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    // resend-throttle: Maximum one verification email per account per window.
    // In-memory (single instance) — a distributed limiter is a documented future improvement.
    private static final long RESEND_THROTTLE_SECONDS = 60;
    private final ConcurrentHashMap<Long, Instant> lastVerificationSentAt = new ConcurrentHashMap<>();

    // OTP brute-force protection: a 6-digit code has only 1M combinations, so the inline
    // verification path allows at most MAX_OTP_ATTEMPTS wrong guesses per account before
    // a lockout window. In-memory (single instance), consistent with the resend throttle.
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long OTP_LOCKOUT_SECONDS = 60;
    private final ConcurrentHashMap<Long, OtpAttemptState> otpAttempts = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final VerificationProperties verificationProperties;

    @Transactional
    @Override
    public String createVerification(Long userId) {
        // A new request supersedes every outstanding code for this user
        tokenRepository.invalidateByUser(userId);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUserId(userId);

        String otp = VerificationTokenGenerator.generateOtp();
        token.setOtpHash(VerificationTokenGenerator.hash(otp));
        token.setExpiresAt(LocalDateTime.now().plus(verificationProperties.getTokenTtlMinutes(), ChronoUnit.MINUTES));
        token.setUsed(false);

        tokenRepository.save(token);
        log.info("Verification code issued for userId={}, expiresAt={}", userId, token.getExpiresAt());
        return otp;
    }

    @Transactional
    @Override
    public User verifyEmailWithCode(String email, String code) {
        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            throw new InvalidVerificationTokenException("Invalid or expired verification code.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid or expired verification code."));

        // Brute-force guard: after MAX_OTP_ATTEMPTS wrong guesses the account is locked
        // out for a short window (the user is told to resend a fresh code).
        OtpAttemptState attempts = otpAttempts.compute(user.getId(), (id, state) -> {
            if (state == null) {
                return new OtpAttemptState();
            }
            // A previous lockout window has fully expired — start a fresh counter and
            // replace the stale entry in place (keeps the map from accumulating inert
            // entries; counters still accumulate across failures within the window).
            return state.isLocked() || state.lockedUntil == null ? state : new OtpAttemptState();
        });
        if (attempts.isLocked()) {
            log.info("OTP verification locked out for userId={}", user.getId());
            throw new InvalidVerificationTokenException(
                    "Too many incorrect attempts. Please resend a new code and try again.");
        }

        EmailVerificationToken verificationToken = tokenRepository
                .findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid or expired verification code."));

        if (verificationToken.getExpiresAt() == null
                || verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationTokenException("Invalid or expired verification code.");
        }

        boolean matches = verificationToken.getOtpHash() != null
                && VerificationTokenGenerator.hash(code).equals(verificationToken.getOtpHash());
        if (!matches) {
            attempts.registerFailure();
            log.info("Invalid verification code for userId={} (attempt {}/{})",
                    user.getId(), attempts.getFailures(), MAX_OTP_ATTEMPTS);
            throw new InvalidVerificationTokenException("Invalid or expired verification code.");
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        // NOTE: there is deliberately NO isEmailVerified() short-circuit here — this is a
        // public endpoint, so the response must be identical for unknown, unverified, and
        // already-verified emails (an already-verified account simply has no active token
        // left and fails the lookup above with the same generic error).
        user.setEmailVerified(true);
        userRepository.save(user);
        otpAttempts.remove(user.getId());

        log.info("Email verified via code for userId={}", user.getId());
        return user;
    }

    @Transactional
    @Override
    public String resendVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            // Unknown account — nothing to send (caller already returns a generic response)
            log.info("Resend verification skipped — no account for userId={}", userId);
            return null;
        }

        if (user.isEmailVerified()) {
            log.info("Resend verification skipped — userId={} is already verified", userId);
            return null;
        }

        // Atomic check-and-set throttle: at most one send per account per window.
        // Stale entries are replaced by the current timestamp, so the map never grows
        // unboundedly for inactive users.
        AtomicBoolean allowed = new AtomicBoolean(false);
        lastVerificationSentAt.compute(userId, (id, lastSent) -> {
            if (lastSent != null && lastSent.plusSeconds(RESEND_THROTTLE_SECONDS).isAfter(Instant.now())) {
                return lastSent; // throttled — keep the original timestamp
            }
            allowed.set(true);
            return Instant.now();
        });

        if (!allowed.get()) {
            log.info("Resend verification rate-limited for userId={}", userId);
            return null;
        }

        // A fresh code also clears any failed-attempt lockout from the previous code
        otpAttempts.remove(userId);

        String otp = createVerification(userId);
        log.info("Verification notification requested for userId={}", userId);
        return otp;
    }

    // OtpAttemptState: In-memory failed-attempt counter with a rolling lockout window.
    // When the window expires the counter restarts from zero on the next failure.
    private static final class OtpAttemptState {
        private int failures;
        private Instant lockedUntil;

        boolean isLocked() {
            return lockedUntil != null && lockedUntil.isAfter(Instant.now());
        }

        int getFailures() {
            return failures;
        }

        void registerFailure() {
            failures++;
            if (failures >= MAX_OTP_ATTEMPTS) {
                lockedUntil = Instant.now().plusSeconds(OTP_LOCKOUT_SECONDS);
                failures = 0; // restart the count once the lockout window begins
            }
        }
    }
}
