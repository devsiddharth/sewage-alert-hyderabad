package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.EventRequest;
import com.sewagealert.community.dto.EventResponse;
import com.sewagealert.community.service.EventService;
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
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Awareness events — CRUD by authorities, registration by citizens")
// EventController: Manages awareness events — CRUD by authorities, registration by citizens
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(
            summary = "Create an awareness event",
            description = "Creates an awareness event. The organizer id comes from the X-Auth-User-Id header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Event created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Parameter(description = "Authenticated organizer's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.createEvent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "List all awareness events",
            description = "Returns all awareness events."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", eventService.getAllEvents()));
    }

    @GetMapping("/upcoming")
    @Operation(
            summary = "List upcoming awareness events",
            description = "Returns events scheduled from today onwards."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Upcoming events retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents() {
        return ResponseEntity.ok(ApiResponse.success("Upcoming events retrieved successfully", eventService.getUpcomingEvents()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get an awareness event by id",
            description = "Returns a single awareness event."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @Parameter(description = "Event id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", eventService.getEvent(id)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an awareness event",
            description = "Updates an existing awareness event."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @Parameter(description = "Event id", example = "1") @PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", eventService.updateEvent(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an awareness event",
            description = "Deletes an awareness event."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Event deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @Parameter(description = "Event id", example = "1") @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @PostMapping("/{id}/register")
    @Operation(
            summary = "Register for an awareness event",
            description = "Registers the authenticated citizen for an event. The user id comes from the "
                    + "X-Auth-User-Id header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registered for event successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Event is full or registration closed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    // POST /api/v1/events/{id}/register: Citizen registers for an event
    public ResponseEntity<ApiResponse<Void>> registerForEvent(
            @Parameter(description = "Event id", example = "1") @PathVariable Long id,
            @Parameter(description = "Authenticated citizen's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId) {
        eventService.registerForEvent(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Registered for event successfully", null));
    }
}
