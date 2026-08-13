package com.sewagealert.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// EmailVerificationToken: Single-use, expiring 6-digit verification code used to prove
// ownership of an email address during registration (OTP-only — no emailed link exists).
//
// Security model:
//   • Only a SHA-256 hash of the code is persisted — the raw code lives exclusively
//     in the verification email sent to the customer (never in the database or logs).
//   • Codes expire (default 30 minutes) and are marked used after successful verification.
//   • A new code request invalidates all previous codes for the same user.
// The userId column is deliberately not a JPA relationship — this service follows the
// project's loose-coupling convention (same as Notification.userId).
@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_evt_user_id", columnList = "user_id"),
        @Index(name = "idx_evt_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // SHA-256 hex digest of the 6-digit verification code (64 chars) — the code the
    // customer types into the registration flow. Never stored in plain text.
    @Column(name = "otp_hash", length = 64)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
