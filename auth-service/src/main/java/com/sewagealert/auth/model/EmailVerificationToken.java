package com.sewagealert.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// EmailVerificationToken: Single-use, expiring token used to prove ownership of an
// email address during registration.
//
// Security model:
//   • Only a SHA-256 hash of the token is persisted — the raw token lives exclusively
//     in the verification link sent to the customer (never in the database or logs).
//   • Tokens expire (default 30 minutes) and are marked used after successful verification.
//   • A new token request invalidates all previous tokens for the same user.
// The userId column is deliberately not a JPA relationship — this service follows the
// project's loose-coupling convention (same as Notification.userId).
@Entity
@Table(name = "email_verification_tokens", indexes = {
        @Index(name = "idx_evt_token_hash", columnList = "token_hash", unique = true),
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

    // SHA-256 hex digest of the raw verification token (64 chars) — never the token itself
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

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
