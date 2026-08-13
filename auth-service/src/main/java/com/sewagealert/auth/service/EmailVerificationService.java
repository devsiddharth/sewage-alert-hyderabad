package com.sewagealert.auth.service;

import com.sewagealert.auth.model.User;

public interface EmailVerificationService {

    // createVerification: Generates a single-use 6-digit code for the user, persists only
    // its SHA-256 hash (invalidating any previous codes), and returns the raw code so the
    // caller can publish it in the registration event. The raw code is never persisted.
    String createVerification(Long userId);

    // verifyEmailWithCode: Validates the 6-digit code the customer typed into the
    // registration flow (email + code lookup, expiry check, attempt throttling) and flips
    // the user's emailVerified flag. Throws InvalidVerificationTokenException for unknown
    // emails, wrong codes, or expired codes. Returns the verified user.
    User verifyEmailWithCode(String email, String code);

    // resendVerification: Issues a fresh verification code for the user (superseding old
    // ones) and returns it, or null when nothing should be sent (unknown email, already
    // verified, or rate-limited). The caller publishes the event.
    String resendVerification(Long userId);
}
