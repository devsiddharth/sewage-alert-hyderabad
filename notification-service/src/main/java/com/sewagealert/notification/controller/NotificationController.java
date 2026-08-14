package com.sewagealert.notification.controller;

import com.sewagealert.notification.dto.ApiResponse;
import com.sewagealert.notification.dto.NotificationResponse;
import com.sewagealert.notification.dto.PagedResponse;
import com.sewagealert.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notifications", description = "In-app notifications for the logged-in user — every query is scoped to the X-Auth-User-Id header")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "List the logged-in user's notifications",
            description = "Paginated, newest-first list of the authenticated user's notifications. The user id "
                    + "is always read from the X-Auth-User-Id header (set by the gateway) — frontend-supplied "
                    + "ids are never trusted. Page size is clamped to 1..100."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    // GET /api/v1/notifications: Paginated, newest-first list of the logged-in user's notifications
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Parameter(description = "Zero-based page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (clamped to 1..100)", example = "20") @RequestParam(defaultValue = "20") int size) {
        // Clamp pagination inputs — never let a client request unbounded pages
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<NotificationResponse> result = notificationService.getNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", result));
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "Get the unread notification count",
            description = "Returns the number of unread notifications for the authenticated user (powers UI badges)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unread count retrieved successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    // GET /api/v1/notifications/unread-count: Number of unread notifications (powers UI badges)
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(
            summary = "Mark a notification as read",
            description = "Marks a single notification as read. Ownership is enforced server-side via the "
                    + "X-Auth-User-Id header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found or not owned by the user")
    })
    @SecurityRequirement(name = "bearerAuth")
    // PATCH /api/v1/notifications/{id}/read: Marks a single notification as read
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @Parameter(description = "Notification id", example = "1") @PathVariable Long id,
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId) {
        NotificationResponse response = notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/read-all")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks every unread notification of the authenticated user as read."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All notifications marked as read")
    })
    @SecurityRequirement(name = "bearerAuth")
    // PATCH /api/v1/notifications/read-all: Marks every unread notification as read
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId) {
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a notification",
            description = "Soft-deletes a single notification. Ownership is enforced server-side via the "
                    + "X-Auth-User-Id header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found or not owned by the user")
    })
    @SecurityRequirement(name = "bearerAuth")
    // DELETE /api/v1/notifications/{id}: Soft-deletes a single notification (user-scoped).
    // Admin-only enforcement happens at the gateway; the service enforces ownership.
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @Parameter(description = "Notification id", example = "1") @PathVariable Long id,
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId) {
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }
}
