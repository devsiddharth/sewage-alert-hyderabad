package com.sewagealert.auth.exception;

// ForbiddenException: Thrown when an authenticated user attempts an operation their
// role does not permit — mapped to HTTP 403 by the global exception handler.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
