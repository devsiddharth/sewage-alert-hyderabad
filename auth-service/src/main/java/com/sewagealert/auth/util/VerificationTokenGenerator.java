package com.sewagealert.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

// VerificationTokenGenerator: Cryptographically secure, unpredictable verification tokens.
//
//   • 32 random bytes (256 bits) from SecureRandom → base64url (43 chars, URL-safe).
//   • The stored representation is a SHA-256 hex digest, so a database leak never
//     exposes a usable token.
public final class VerificationTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private VerificationTokenGenerator() {}

    // generate: Returns a URL-safe random token — never derived from user id, email,
    // password, or any predictable input.
    public static String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // hash: SHA-256 hex digest of the raw token (what actually gets persisted).
    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is mandated by the JVM — this can never happen in practice
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }
}
