package com.sewagealert.notification.exception;

// RabbitMqPublishException: Thrown when an outbound publish to RabbitMQ fails
// (e.g., broker unavailable, channel error). Producers in other services treat publish
// failures as non-fatal and log them so core business flows stay resilient.
public class RabbitMqPublishException extends RuntimeException {

    public RabbitMqPublishException(String message) {
        super(message);
    }

    public RabbitMqPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
