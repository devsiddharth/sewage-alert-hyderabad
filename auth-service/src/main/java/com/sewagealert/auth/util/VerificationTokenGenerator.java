package com.sewagealert.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

// VerificationTokenGenerator: Cryptographically secure, unpredictable 6-digit OTPs.
//
//   • 6 digits from SecureRandom (100000–999999 — never a leading zero).
//   • The stored representation is a SHA-256 hex digest, so a database leak never
//     exposes a usable code.
public final class VerificationTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private VerificationTokenGenerator() {}

    // generateOtp: Returns a 6-digit numeric one-time passcode — short enough to type
    // into the registration flow. The range 100000–999999 never produces a leading zero,
    // avoiding ambiguity when the user copies or types the code.
    public static String generateOtp() {
        int code = 100_000 + SECURE_RANDOM.nextInt(900_000);
        return String.valueOf(code);
    }

    // hash: SHA-256 hex digest of the raw code (what actually gets persisted).
    public static String hash(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is mandated by the JVM — this can never happen in practice
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }
}
