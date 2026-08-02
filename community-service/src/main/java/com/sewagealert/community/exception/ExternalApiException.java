package com.sewagealert.community.exception;

/**
 * ExternalApiException: Base exception for all failures while calling external/public APIs.
 * Subclasses add provider-specific context. Raw Feign exceptions are never exposed to callers —
 * they are translated into these exceptions by the integration services.
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
