package com.sewagealert.notification.dto;

import com.sewagealert.notification.model.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

// NotificationResponse: DTO returned to API consumers — never exposes the internal entity directly
@Data
@NoArgsConstructor
@Schema(description = "Notification DTO returned to API consumers — never exposes the internal entity directly")
public class NotificationResponse {

    @Schema(description = "Notification id", example = "1")
    private Long id;
    @Schema(description = "User the notification belongs to (auth-service id)", example = "1")
    private Long userId;

    @Schema(description = "Notification title", example = "Complaint status updated")
    private String title;

    @Schema(description = "Notification message body")
    private String message;

    @Schema(description = "Notification type", example = "COMPLAINT_STATUS_UPDATED")
    private String notificationType;

    @Schema(description = "Optional id of the referenced domain object (e.g. complaint id)", example = "42")
    private Long referenceId;

    @Schema(description = "Optional type of the referenced domain object", example = "COMPLAINT")
    private String referenceType;

    @Schema(description = "Whether the notification has been read", example = "false")
    private boolean read;

    @Schema(description = "Timestamp when the notification was marked read")
    private LocalDateTime readAt;

    @Schema(description = "Extensible JSON metadata bag")
    private Map<String, Object> metadata;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
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
