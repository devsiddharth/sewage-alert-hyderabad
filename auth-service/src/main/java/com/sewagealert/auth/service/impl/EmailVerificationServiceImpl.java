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

// EmailVerificationServiceImpl: Owns the verification-token lifecycle — issuance,
// validation, invalidation and throttling. Never logs raw tokens.
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    // resend-throttle: Maximum one verification email per account per window.
    // In-memory (single instance) — a distributed limiter is a documented future improvement.
    private static final long RESEND_THROTTLE_SECONDS = 60;
    private final ConcurrentHashMap<Long, Instant> lastVerificationSentAt = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final VerificationProperties verificationProperties;

    @Transactional
    @Override
    public String createVerificationToken(Long userId) {
        // A new request supersedes every outstanding token for this user
        tokenRepository.invalidateByUser(userId);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUserId(userId);

        String rawToken = VerificationTokenGenerator.generate();
        token.setTokenHash(VerificationTokenGenerator.hash(rawToken));
        token.setExpiresAt(LocalDateTime.now().plus(verificationProperties.getTokenTtlMinutes(), ChronoUnit.MINUTES));
        token.setUsed(false);

        tokenRepository.save(token);
        log.info("Verification token issued for userId={}, expiresAt={}", userId, token.getExpiresAt());
        return rawToken;
    }

    @Transactional
    @Override
    public User verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidVerificationTokenException("Invalid or expired verification link.");
        }

        EmailVerificationToken verificationToken = tokenRepository
                .findByTokenHash(VerificationTokenGenerator.hash(token))
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid or expired verification link."));

        // Single generic failure message: never reveal whether the token existed, was used,
        // or expired (avoids a timing/enumeration side-channel).
        if (verificationToken.isUsed()
                || verificationToken.getExpiresAt() == null
                || verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationTokenException("Invalid or expired verification link.");
        }

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid or expired verification link."));

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified for userId={}", user.getId());
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

        String rawToken = createVerificationToken(userId);
        log.info("Verification notification requested for userId={}", userId);
        return rawToken;
    }
}
