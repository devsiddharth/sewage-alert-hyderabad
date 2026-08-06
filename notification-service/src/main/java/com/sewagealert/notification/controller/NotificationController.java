package com.sewagealert.notification.controller;

import com.sewagealert.notification.dto.ApiResponse;
import com.sewagealert.notification.dto.NotificationResponse;
import com.sewagealert.notification.dto.PagedResponse;
import com.sewagealert.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// NotificationController: REST API for the logged-in user's notifications.
//
// The authenticated user id is always read from the gateway-provided X-Auth-User-Id header
// (same convention as complaint-service/user-service). Frontend-supplied user ids are never
// trusted — every query is scoped to the header value, so users can only see their own data.
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    // GET /api/v1/notifications: Paginated, newest-first list of the logged-in user's notifications
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Clamp pagination inputs — never let a client request unbounded pages
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<NotificationResponse> result = notificationService.getNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", result));
    }

    @GetMapping("/unread-count")
    // GET /api/v1/notifications/unread-count: Number of unread notifications (powers UI badges)
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    @PatchMapping("/{id}/read")
    // PATCH /api/v1/notifications/{id}/read: Marks a single notification as read
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        NotificationResponse response = notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/read-all")
    // PATCH /api/v1/notifications/read-all: Marks every unread notification as read
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", updated));
    }

    @DeleteMapping("/{id}")
    // DELETE /api/v1/notifications/{id}: Soft-deletes a single notification (user-scoped).
    // Admin-only enforcement happens at the gateway; the service enforces ownership.
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long userId) {
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }
}
