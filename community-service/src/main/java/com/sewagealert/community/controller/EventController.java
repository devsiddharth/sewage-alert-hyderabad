package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.EventRequest;
import com.sewagealert.community.dto.EventResponse;
import com.sewagealert.community.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
// EventController: Manages awareness events — CRUD by authorities, registration by citizens
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.createEvent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", eventService.getAllEvents()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents() {
        return ResponseEntity.ok(ApiResponse.success("Upcoming events retrieved successfully", eventService.getUpcomingEvents()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", eventService.getEvent(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", eventService.updateEvent(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @PostMapping("/{id}/register")
    // POST /api/v1/events/{id}/register: Citizen registers for an event
    public ResponseEntity<ApiResponse<Void>> registerForEvent(
            @PathVariable Long id, @RequestHeader("X-Auth-User-Id") Long userId) {
        eventService.registerForEvent(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Registered for event successfully", null));
    }
}
