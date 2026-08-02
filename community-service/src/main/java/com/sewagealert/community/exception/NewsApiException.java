package com.sewagealert.community.exception;

/**
 * NewsApiException: Raised when the news provider (GNews) request fails or returns an error.
 */
public class NewsApiException extends ExternalApiException {

    public NewsApiException(String message) {
        super(message);
    }

    public NewsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
