package com.sewagealert.user.exception;

// UserProfileNotFoundException: Custom runtime exception thrown when a requested user profile cannot be found
// Extends RuntimeException so it can be caught by @RestControllerAdvice for consistent error responses
public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
}
