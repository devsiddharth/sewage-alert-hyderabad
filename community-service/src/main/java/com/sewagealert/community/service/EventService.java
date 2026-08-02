package com.sewagealert.community.service;

import com.sewagealert.community.dto.EventRequest;
import com.sewagealert.community.dto.EventResponse;

import java.util.List;

// EventService: Business logic for awareness events — CRUD by authorities, registration by citizens
public interface EventService {

    // createEvent: Creates a new event organized by the given organizer
    EventResponse createEvent(Long organizerId, EventRequest request);

    // getEvent: Retrieves a single event by its ID
    EventResponse getEvent(Long eventId);

    // getAllEvents: Returns all events in the system
    List<EventResponse> getAllEvents();

    // getUpcomingEvents: Returns all events scheduled after today
    List<EventResponse> getUpcomingEvents();

    // updateEvent: Updates an existing event's details
    EventResponse updateEvent(Long eventId, EventRequest request);

    // deleteEvent: Removes an event by its ID
    void deleteEvent(Long eventId);

    // registerForEvent: Allows a citizen to register for an event — checks for duplicates and capacity
    void registerForEvent(Long eventId, Long userId);

    // getEventsByOrganizer: Returns all events organized by a specific organizer
    List<EventResponse> getEventsByOrganizer(Long organizerId);
}
