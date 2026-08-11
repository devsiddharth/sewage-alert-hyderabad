package com.sewagealert.auth.exception;

// InvalidVerificationTokenException: Raised by the email verification flow when the
// submitted token is unknown, expired, or already used. Mapped to HTTP 400 with code
// INVALID_VERIFICATION_TOKEN by the GlobalExceptionHandler.
public class InvalidVerificationTokenException extends RuntimeException {

    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}
