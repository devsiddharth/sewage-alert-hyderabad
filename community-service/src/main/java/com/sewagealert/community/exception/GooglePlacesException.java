package com.sewagealert.community.exception;

/**
 * GooglePlacesException: Raised when the Google Places API request fails,
 * returns a non-OK status, or the API key is not configured.
 */
public class GooglePlacesException extends ExternalApiException {

    public GooglePlacesException(String message) {
        super(message);
    }

    public GooglePlacesException(String message, Throwable cause) {
        super(message, cause);
    }
}
