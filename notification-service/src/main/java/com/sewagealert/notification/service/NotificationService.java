package com.sewagealert.notification.service;

import com.sewagealert.notification.dto.NotificationEvent;
import com.sewagealert.notification.dto.NotificationResponse;
import com.sewagealert.notification.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

// NotificationService: Core business logic — event processing (from RabbitMQ) and
// notification retrieval/mutation (from the REST API). Only the consumer writes
// notifications; the API layer only reads and updates flags.
public interface NotificationService {

    // processEvent: Validates an inbound RabbitMQ event and persists it as a notification.
    // Throws NotificationProcessingException for permanent payload failures (→ DLQ).
    void processEvent(NotificationEvent event);

    // getNotificationsForUser: Paginated, newest-first feed of the logged-in user's notifications
    PagedResponse<NotificationResponse> getNotificationsForUser(Long userId, Pageable pageable);

    // getUnreadCount: Number of unread notifications for the logged-in user (badge support)
    long getUnreadCount(Long userId);

    // markAsRead: Marks a single notification as read (ownership enforced)
    NotificationResponse markAsRead(Long userId, Long notificationId);

    // markAllAsRead: Marks every unread notification of the user as read (bulk UPDATE)
    int markAllAsRead(Long userId);

    // deleteNotification: Soft-deletes a single notification (ownership enforced)
    void deleteNotification(Long userId, Long notificationId);
}
