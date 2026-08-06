package com.sewagealert.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// NotificationEvent: The canonical event contract exchanged over RabbitMQ.
// Producers (Complaint Service, Community Service, future Admin tooling) publish this
// payload with a matching routing key; this service consumes, validates, and persists it.
// It is JSON-serialized via Jackson (Jackson2JsonMessageConverter) and must stay
// backwards-compatible — add optional fields rather than renaming existing ones.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    // eventId: Unique UUID assigned by the producer — used for tracing/dedup (future-ready)
    private String eventId;

    // eventType: Must match a NotificationType enum name (e.g., COMPLAINT_CREATED)
    private String eventType;

    // userId: The recipient (auth-service user id) — NEVER taken from the frontend
    private Long userId;

    // complaintId: Convenience reference for complaint-related events (null otherwise)
    private Long complaintId;

    // referenceId/referenceType: Generic reference for any domain object
    // (e.g., referenceType="EVENT", referenceId=<eventId> for community events)
    private String referenceType;
    private Long referenceId;

    private String title;
    private String message;

    // status/priority: Optional snapshot of the domain object state (e.g., complaint status)
    private String status;
    private String priority;

    private LocalDateTime createdAt;

    // metadata: Free-form JSON-safe key/value bag for extensible event data
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
