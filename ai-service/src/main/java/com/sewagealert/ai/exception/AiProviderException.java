package com.sewagealert.ai.exception;

/**
 * AiProviderException: Thrown when the AI provider (OpenAI-compatible API) fails.
 * Covers: API unavailable, timeout, rate limit, invalid response, empty response.
 */
public class AiProviderException extends RuntimeException {

    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
