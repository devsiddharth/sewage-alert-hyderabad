package com.sewagealert.notification.util;

import com.sewagealert.notification.enums.NotificationType;
import com.sewagealert.notification.exception.NotificationProcessingException;

// EventTypeResolver: Maps the string eventType carried in the RabbitMQ payload to the
// NotificationType enum. Unknown or blank event types are treated as permanent (non-retryable)
// payload failures so poison messages go straight to the DLQ instead of retrying forever.
public final class EventTypeResolver {

    private EventTypeResolver() {}

    public static NotificationType resolve(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new NotificationProcessingException("Notification event is missing eventType");
        }
        try {
            return NotificationType.valueOf(eventType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new NotificationProcessingException(
                    "Unknown notification eventType: " + eventType + " — supported types: "
                            + java.util.Arrays.toString(NotificationType.values()));
        }
    }
}
