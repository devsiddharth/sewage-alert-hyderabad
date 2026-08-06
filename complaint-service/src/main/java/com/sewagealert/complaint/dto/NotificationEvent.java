package com.sewagealert.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// NotificationEvent: JSON event contract published to RabbitMQ and consumed by the
// Notification Service. The field layout mirrors the notification-service DTO exactly —
// this is the inter-service contract, so changes must be rolled out compatibly.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    // eventId: Unique UUID per publish — enables tracing and future deduplication
    private String eventId;

    // eventType: Must match a NotificationType enum name in the Notification Service
    // (COMPLAINT_CREATED, COMPLAINT_ASSIGNED, COMPLAINT_STATUS_UPDATED,
    //  COMPLAINT_RESOLVED, COMPLAINT_REJECTED, COMPLAINT_REOPENED, ...)
    private String eventType;

    // userId: Recipient = the citizen who owns the complaint (auth-service user id)
    private Long userId;

    private Long complaintId;

    // referenceId/referenceType: Generic reference (kept for community events/announcements)
    private String referenceType;
    private Long referenceId;

    private String title;
    private String message;

    // status/priority: Snapshot of the complaint state at publish time
    private String status;
    private String priority;

    private LocalDateTime createdAt;

    // metadata: Extensible JSON-safe bag (remarks, links, ...)
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
