package com.sewagealert.auth.exception;

// EmailNotVerifiedException: Raised at login when the account's email address has not
// been verified yet — the citizen must click the link from the verification email first.
// Mapped to HTTP 401 with code EMAIL_NOT_VERIFIED by the GlobalExceptionHandler.
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
