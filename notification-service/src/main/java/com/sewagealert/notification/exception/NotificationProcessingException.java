package com.sewagealert.notification.exception;

// NotificationProcessingException: Thrown when a consumed RabbitMQ event is malformed or
// cannot be processed (missing eventType, unknown event type, invalid payload). These are
// permanent failures — the consumer rejects the message so it lands in the DLQ without retry.
public class NotificationProcessingException extends RuntimeException {

    public NotificationProcessingException(String message) {
        super(message);
    }

    public NotificationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
