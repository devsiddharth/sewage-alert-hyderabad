package com.sewagealert.notification.dto;

import com.sewagealert.notification.model.Notification;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

// NotificationResponse: DTO returned to API consumers — never exposes the internal entity directly
@Data
@NoArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String notificationType;
    private Long referenceId;
    private String referenceType;
    private boolean read;
    private LocalDateTime readAt;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // fromEntity: Converts the JPA entity into a clean response DTO
    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setNotificationType(notification.getNotificationType().name());
        response.setReferenceId(notification.getReferenceId());
        response.setReferenceType(notification.getReferenceType());
        response.setRead(notification.isRead());
        response.setReadAt(notification.getReadAt());
        response.setMetadata(notification.getMetadata());
        response.setCreatedAt(notification.getCreatedAt());
        response.setUpdatedAt(notification.getUpdatedAt());
        return response;
    }
}
