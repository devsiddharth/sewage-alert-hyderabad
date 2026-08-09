package com.sewagealert.complaint.exception;

/**
 * InvalidImageException: Thrown when an uploaded image fails client-side validation —
 * empty file, unsupported content type, or exceeding the configured maximum size.
 * Maps to HTTP 400 in GlobalExceptionHandler.
 */
public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String message) {
        super(message);
    }
}
