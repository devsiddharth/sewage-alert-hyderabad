package com.sewagealert.complaint.exception;

// UserServiceUnavailableException: Thrown when User Service cannot be reached or fails
// during OpenFeign communication — maps to HTTP 503 in GlobalExceptionHandler.
public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
