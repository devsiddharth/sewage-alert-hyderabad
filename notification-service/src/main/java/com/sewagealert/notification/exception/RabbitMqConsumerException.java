package com.sewagealert.notification.exception;

// RabbitMqConsumerException: Thrown when the RabbitMQ consumer encounters an unexpected
// error while listening (connection loss, unexpected listener failure). Transient failures
// are retried by the container-level retry policy before the message is sent to the DLQ.
public class RabbitMqConsumerException extends RuntimeException {

    public RabbitMqConsumerException(String message) {
        super(message);
    }

    public RabbitMqConsumerException(String message, Throwable cause) {
        super(message, cause);
    }
}
