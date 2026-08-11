package com.sewagealert.notification.service.impl;

import com.sewagealert.notification.dto.NotificationEvent;
import com.sewagealert.notification.dto.NotificationResponse;
import com.sewagealert.notification.dto.PagedResponse;
import com.sewagealert.notification.enums.NotificationType;
import com.sewagealert.notification.exception.NotificationNotFoundException;
import com.sewagealert.notification.exception.NotificationProcessingException;
import com.sewagealert.notification.mapper.NotificationMapper;
import com.sewagealert.notification.model.Notification;
import com.sewagealert.notification.producer.NotificationEventProducer;
import com.sewagealert.notification.repository.NotificationRepository;
import com.sewagealert.notification.service.EmailNotificationService;
import com.sewagealert.notification.service.NotificationService;
import com.sewagealert.notification.util.EventTypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// NotificationServiceImpl: Consumes validated RabbitMQ events (via the consumer), persists
// them, and serves them through the REST API. All operations are scoped to the authenticated
// user id extracted from the gateway header — never from frontend-supplied ids.
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventProducer notificationEventProducer;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    @Override
    // processEvent: Entry point for the RabbitMQ consumer. Validates the payload, persists
    // the notification, then re-publishes for future delivery channels.
    public void processEvent(NotificationEvent event) {
        validateEvent(event);

        NotificationType type = EventTypeResolver.resolve(event.getEventType());

        // The raw verification token must never be persisted: it is only needed at send-time
        // to build the verification link, so the stored copy drops it (the email service still
        // receives the original event with the token).
        NotificationEvent persistedEvent = stripVerificationToken(event);
        Notification notification = NotificationMapper.toEntity(persistedEvent, type);

        notification = notificationRepository.save(notification);
        log.info("Notification stored — id: {}, userId: {}, type: {} (eventId: {})",
                notification.getId(), notification.getUserId(), type, event.getEventId());

        // Transactional email dispatch for auth events — the Notification Service is the
        // single owner of outgoing email (via EmailJS). Failures are contained inside the
        // email service so the stored notification / event ack are never compromised.
        if (type == NotificationType.USER_REGISTERED
                || type == NotificationType.EMAIL_VERIFICATION_REQUESTED) {
            emailNotificationService.sendVerificationEmail(event);
        } else if (type == NotificationType.EMAIL_VERIFIED) {
            emailNotificationService.sendWelcomeEmail(event);
        }

        // Future-ready: fan out to email/SMS/push workers (fire-and-forget)
        notificationEventProducer.publishStoredNotification(notification);
    }

    // validateEvent: Structural validation of the inbound payload. Failures here are
    // permanent (bad producer or poison message) — the consumer rejects them to the DLQ.
    private void validateEvent(NotificationEvent event) {
        if (event == null) {
            throw new NotificationProcessingException("Received a null notification event");
        }
        if (event.getUserId() == null) {
            throw new NotificationProcessingException("Notification event is missing userId");
        }
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new NotificationProcessingException("Notification event is missing title");
        }
        if (event.getMessage() == null || event.getMessage().isBlank()) {
            throw new NotificationProcessingException("Notification event is missing message");
        }
        // eventType validity is checked by EventTypeResolver
    }

    // stripVerificationToken: Returns an event copy whose metadata no longer contains the
    // single-use verification token — the token travels only from producer → consumer → EmailJS.
    private NotificationEvent stripVerificationToken(NotificationEvent event) {
        if (event.getMetadata() == null || event.getMetadata().isEmpty()) {
            return event;
        }
        Map<String, Object> metadata = new HashMap<>(event.getMetadata());
        metadata.remove("verificationToken");

        return NotificationEvent.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .userId(event.getUserId())
                .complaintId(event.getComplaintId())
                .referenceType(event.getReferenceType())
                .referenceId(event.getReferenceId())
                .title(event.getTitle())
                .message(event.getMessage())
                .status(event.getStatus())
                .priority(event.getPriority())
                .createdAt(event.getCreatedAt())
                .metadata(metadata)
                .build();
    }

    @Override
    // getNotificationsForUser: Paginated feed, newest first. The sort is applied by the
    // controller's Pageable (Sort.by("createdAt").descending()) — no extra DB round trips.
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotificationsForUser(Long userId, Pageable pageable) {
        return PagedResponse.fromPage(
                notificationRepository.findByUserIdAndDeletedFalse(userId, pageable),
                NotificationMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
    }

    @Transactional
    @Override
    // markAsRead: Marks one notification read — the ownership condition lives in the query,
    // so a user can never read someone else's notification (404 instead).
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserIdAndDeletedFalse(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found with id: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            log.info("Notification {} marked as read by userId: {}", notificationId, userId);
        }
        return NotificationMapper.toResponse(notification);
    }

    @Transactional
    @Override
    // markAllAsRead: Single bulk UPDATE — avoids N+1 individual loads/saves at high volume
    public int markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("Marked {} notifications as read for userId: {}", updated, userId);
        return updated;
    }

    @Transactional
    @Override
    // deleteNotification: Soft delete — the row is flagged and hidden from all queries.
    public void deleteNotification(Long userId, Long notificationId) {
        int deleted = notificationRepository.softDelete(notificationId, userId, LocalDateTime.now());
        if (deleted == 0) {
            throw new NotificationNotFoundException(
                    "Notification not found with id: " + notificationId);
        }
        log.info("Notification {} soft-deleted by userId: {}", notificationId, userId);
    }
}
