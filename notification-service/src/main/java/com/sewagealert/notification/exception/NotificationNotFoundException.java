package com.sewagealert.notification.exception;

// NotificationNotFoundException: Thrown when a notification does not exist, belongs to
// another user, or has been soft-deleted. Maps to HTTP 404.
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(String message) {
        super(message);
    }
}
