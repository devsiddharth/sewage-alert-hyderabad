package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.EventRequest;
import com.sewagealert.community.dto.EventResponse;
import com.sewagealert.community.model.Event;
import com.sewagealert.community.model.EventRegistration;
import com.sewagealert.community.repository.EventRegistrationRepository;
import com.sewagealert.community.repository.EventRepository;
import com.sewagealert.community.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// EventServiceImpl: Core business logic for awareness events — CRUD by authorities, registration by citizens
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    @Override
    // createEvent: Creates a new event organized by the given organizer
    public EventResponse createEvent(Long organizerId, EventRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setOrganizerName(request.getOrganizerName());
        event.setOrganizerId(organizerId);
        event.setCapacity(request.getCapacity());

        event = eventRepository.save(event);
        log.info("Event created: {} by organizer: {}", event.getTitle(), organizerId);

        return EventResponse.fromEntity(event);
    }

    @Override
    // getEvent: Retrieves a single event by its ID
    public EventResponse getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return EventResponse.fromEntity(event);
    }

    @Override
    // getAllEvents: Returns all events in the system
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // getUpcomingEvents: Returns all events scheduled after today
    public List<EventResponse> getUpcomingEvents() {
        return eventRepository.findByEventDateAfter(LocalDate.now()).stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // updateEvent: Updates an existing event's details
    public EventResponse updateEvent(Long eventId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setOrganizerName(request.getOrganizerName());
        event.setCapacity(request.getCapacity());

        event = eventRepository.save(event);
        log.info("Event updated: {}", eventId);

        return EventResponse.fromEntity(event);
    }

    @Override
    // deleteEvent: Removes an event by its ID
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
        log.info("Event deleted: {}", eventId);
    }

    @Transactional
    @Override
    // registerForEvent: Allows a citizen to register for an event — checks for duplicates and capacity
    public void registerForEvent(Long eventId, Long userId) {
        // Check if already registered
        if (registrationRepository.findByEventIdAndUserId(eventId, userId).isPresent()) {
            throw new RuntimeException("User already registered for this event");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Check capacity
        if (event.getCapacity() != null) {
            long currentRegistrations = registrationRepository.findByEventId(eventId).size();
            if (currentRegistrations >= event.getCapacity()) {
                throw new RuntimeException("Event is at full capacity");
            }
        }

        EventRegistration registration = new EventRegistration(userId);
        event.addRegistration(registration);
        eventRepository.save(event);

        log.info("User {} registered for event {}", userId, eventId);
    }

    @Override
    // getEventsByOrganizer: Returns all events organized by a specific organizer
    public List<EventResponse> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
