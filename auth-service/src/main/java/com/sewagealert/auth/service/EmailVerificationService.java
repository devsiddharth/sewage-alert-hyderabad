package com.sewagealert.auth.service;

import com.sewagealert.auth.model.User;

public interface EmailVerificationService {

    // createVerificationToken: Generates a secure token for the user, persists only its
    // SHA-256 hash (invalidating any previous tokens), and returns the raw token so the
    // caller can embed it in the verification link / publish it in the registration event.
    String createVerificationToken(Long userId);

    // verifyEmail: Validates the submitted token (exists, not expired, not used), marks it
    // used, and flips the user's emailVerified flag. Throws InvalidVerificationTokenException
    // for unknown/expired/used tokens. Returns the verified user.
    User verifyEmail(String token);

    // resendVerification: Issues a fresh verification token for the user (superseding old
    // ones) and returns it, or null when nothing should be sent (unknown email, already
    // verified, or rate-limited). The caller publishes the event / returns a generic response.
    String resendVerification(Long userId);
}
