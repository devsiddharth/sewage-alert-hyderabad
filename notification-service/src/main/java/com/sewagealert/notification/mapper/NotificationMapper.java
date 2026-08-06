package com.sewagealert.notification.mapper;

import com.sewagealert.notification.dto.NotificationEvent;
import com.sewagealert.notification.dto.NotificationResponse;
import com.sewagealert.notification.enums.NotificationType;
import com.sewagealert.notification.model.Notification;

import java.util.HashMap;

// NotificationMapper: Central mapping between the RabbitMQ event contract, the JPA entity,
// and the API response DTO. Keeps transformations in one place so the consumer and the
// controller never touch entity internals directly.
public final class NotificationMapper {

    private NotificationMapper() {}

    // toEntity: Builds a Notification from a validated event. The eventType is resolved
    // by the consumer before calling this method (see EventTypeResolver).
    public static Notification toEntity(NotificationEvent event, NotificationType type) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setTitle(event.getTitle());
        notification.setMessage(event.getMessage());
        notification.setNotificationType(type);

        // Prefer the generic reference fields; fall back to the complaintId convenience field
        if (event.getReferenceId() != null) {
            notification.setReferenceId(event.getReferenceId());
            notification.setReferenceType(event.getReferenceType() != null
                    ? event.getReferenceType()
                    : "COMPLAINT");
        } else if (event.getComplaintId() != null) {
            notification.setReferenceId(event.getComplaintId());
            notification.setReferenceType("COMPLAINT");
        }

        notification.setMetadata(event.getMetadata() != null ? event.getMetadata() : new HashMap<>());
        notification.setRead(false);
        notification.setDeleted(false);
        return notification;
    }

    // toEvent: Rebuilds the event contract from a stored notification — used when re-publishing
    // to future delivery channels (email/SMS/push workers).
    public static NotificationEvent toEvent(Notification notification) {
        return NotificationEvent.builder()
                .eventType(notification.getNotificationType().name())
                .userId(notification.getUserId())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .metadata(notification.getMetadata())
                .build();
    }

    // toResponse: Converts the entity into the API response DTO
    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.fromEntity(notification);
    }
}
