package com.sewagealert.community.controller;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.service.NgoEventManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ngo/events")
@Tag(name = "NGO Events", description = "NGO event management, approval workflow and citizen registration")
public class NgoEventController {

    private final NgoEventManagementService eventService;

    public NgoEventController(NgoEventManagementService eventService) {
        this.eventService = eventService;
    }

    // ---- NGO endpoints ----

    @PostMapping
    @Operation(summary = "Create an NGO event", description = "NGO creates an event that goes to PENDING_APPROVAL.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoEventResponse>> createEvent(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody NgoEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created", eventService.createEvent(userId, request)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my NGO events")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoEventResponse>>> getMyEvents(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Events retrieved", eventService.getMyEvents(userId)));
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update my NGO event")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoEventResponse>> updateEvent(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NgoEventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event updated", eventService.updateEvent(userId, eventId, request)));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete my NGO event")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long eventId) {
        eventService.deleteEvent(userId, eventId);
        return ResponseEntity.ok(ApiResponse.success("Event deleted", null));
    }

    // ---- Public endpoints ----

    @GetMapping("/published")
    @Operation(summary = "List published NGO events (public)", description = "Only PUBLISHED events visible to citizens.")
    public ResponseEntity<ApiResponse<List<NgoEventResponse>>> getPublishedEvents() {
        return ResponseEntity.ok(ApiResponse.success("Events retrieved", eventService.getPublishedEvents()));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get NGO event details")
    public ResponseEntity<ApiResponse<NgoEventResponse>> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.success("Event retrieved", eventService.getEvent(eventId)));
    }

    // ---- User registration ----

    @PostMapping("/{eventId}/register")
    @Operation(summary = "Register for an NGO event (citizen)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> registerForEvent(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long eventId) {
        eventService.registerForEvent(userId, eventId);
        return ResponseEntity.ok(ApiResponse.success("Registered for event", null));
    }

    @PostMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel registration for an NGO event")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long eventId) {
        eventService.cancelRegistration(userId, eventId);
        return ResponseEntity.ok(ApiResponse.success("Registration cancelled", null));
    }

    @GetMapping("/my-registrations")
    @Operation(summary = "Get my event registrations (citizen)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoEventResponse>>> getMyRegistrations(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Registrations retrieved", eventService.getMyRegistrations(userId)));
    }

    // ---- Admin endpoints ----

    @GetMapping("/admin/pending")
    @Operation(summary = "List pending NGO events (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoEventResponse>>> getPendingEvents() {
        return ResponseEntity.ok(ApiResponse.success("Pending events retrieved", eventService.getPendingEvents()));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "List all NGO events (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoEventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success("All events retrieved", eventService.getAllEventsForAdmin()));
    }

    @PostMapping("/{eventId}/approve")
    @Operation(summary = "Approve an NGO event (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoEventResponse>> approveEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-Auth-User-Id") Long adminUserId) {
        return ResponseEntity.ok(ApiResponse.success("Event approved", eventService.approveEvent(eventId, adminUserId)));
    }

    @PostMapping("/{eventId}/reject")
    @Operation(summary = "Reject an NGO event (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoEventResponse>> rejectEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-Auth-User-Id") Long adminUserId,
            @RequestBody EventApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event rejected",
                eventService.rejectEvent(eventId, adminUserId, request.getRejectionReason())));
    }

    @GetMapping("/{eventId}/participants")
    @Operation(summary = "Get event participants (NGO sees own events only, admin sees all)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoParticipantResponse>>> getEventParticipants(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long eventId,
            @Parameter(description = "Is admin?") @RequestParam(defaultValue = "false") boolean isAdmin) {
        return ResponseEntity.ok(ApiResponse.success("Participants retrieved",
                eventService.getEventParticipants(userId, eventId, isAdmin)));
    }
}
