package com.sewagealert.auth.dto;

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
//
// For user-auth events only userId/title/message/createdAt/metadata are populated
// (metadata carries the customer name, email, and — for registration — the verification
// token needed by the Notification Service to build the verification link).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    // eventId: Unique UUID per publish — enables tracing and future deduplication
    private String eventId;

    // eventType: Must match a NotificationType enum name in the Notification Service
    // (USER_REGISTERED, EMAIL_VERIFICATION_REQUESTED, EMAIL_VERIFIED, ...)
    private String eventType;

    // userId: Recipient = the auth-service user id
    private Long userId;

    private Long complaintId;

    // referenceId/referenceType: Generic reference (kept for compatibility with the contract)
    private String referenceType;
    private Long referenceId;

    private String title;
    private String message;

    // status/priority: Optional snapshot of the domain object state (unused for auth events)
    private String status;
    private String priority;

    private LocalDateTime createdAt;

    // metadata: Extensible JSON-safe bag (name, email, verificationCode for auth events)
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
