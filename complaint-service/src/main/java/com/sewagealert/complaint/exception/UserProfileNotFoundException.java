package com.sewagealert.complaint.exception;

// UserProfileNotFoundException: Thrown when the auth user has no profile in User Service.
// Prevents creating complaints for non-existent users — maps to HTTP 404 in GlobalExceptionHandler.
public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
}
