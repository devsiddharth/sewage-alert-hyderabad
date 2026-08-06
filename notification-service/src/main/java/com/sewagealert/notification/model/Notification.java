package com.sewagealert.notification.model;

import com.sewagealert.notification.enums.NotificationType;
import com.sewagealert.notification.util.JsonUtils;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;

// Notification: Persisted in-app notification for a user.
// userId references the auth-service user id (not a JPA FK — loose coupling between microservices).
// Soft-delete is supported via the `deleted` flag so user history can be restored and
// delivery channels (email/SMS) can still reference removed in-app items.
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at"),
        @Index(name = "idx_notifications_is_read", columnList = "is_read"),
        @Index(name = "idx_notifications_user_read", columnList = "user_id,is_read")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    // referenceId/referenceType: Which domain object this notification is about
    // (e.g., referenceType="COMPLAINT", referenceId=42). Not a JPA FK.
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // metadata: JSON string holding extensible event data (status snapshot, remarks, links, ...)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    // Soft delete flag — DELETE endpoints mark records as deleted instead of removing rows
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // JSON accessors: expose metadata as a Map at the API level while storing a JSON string
    public Map<String, Object> getMetadata() {
        return JsonUtils.fromJson(metadata);
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = JsonUtils.toJson(metadata);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
