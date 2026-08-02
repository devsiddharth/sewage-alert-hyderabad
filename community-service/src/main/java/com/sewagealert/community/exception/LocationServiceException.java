package com.sewagealert.community.exception;

/**
 * LocationServiceException: Raised when the OpenStreetMap Overpass API request fails,
 * times out, or returns unusable data.
 */
public class LocationServiceException extends ExternalApiException {

    public LocationServiceException(String message) {
        super(message);
    }

    public LocationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
