package com.sewagealert.community.service;

import com.sewagealert.community.dto.*;

import java.util.List;

/**
 * NgoEventManagementService: NGO event CRUD, approval workflow, user registration.
 */
public interface NgoEventManagementService {

    // NGO creates an event
    NgoEventResponse createEvent(Long userId, NgoEventRequest request);

    // NGO gets their own events
    List<NgoEventResponse> getMyEvents(Long userId);

    // NGO updates their own event (only PENDING_APPROVAL or REJECTED status)
    NgoEventResponse updateEvent(Long userId, Long eventId, NgoEventRequest request);

    // NGO deletes their own event
    void deleteEvent(Long userId, Long eventId);

    // Get published events (public - for user discovery)
    List<NgoEventResponse> getPublishedEvents();

    // Get upcoming published events (public)
    List<NgoEventResponse> getUpcomingPublishedEvents();

    // Get event details
    NgoEventResponse getEvent(Long eventId);

    // User registers for an event
    void registerForEvent(Long userId, Long eventId);

    // User cancels registration
    void cancelRegistration(Long userId, Long eventId);

    // Get user's registered events
    List<NgoEventResponse> getMyRegistrations(Long userId);

    // ---- Admin endpoints ----

    // Admin: list pending events
    List<NgoEventResponse> getPendingEvents();

    // Admin: approve event
    NgoEventResponse approveEvent(Long eventId, Long adminUserId);

    // Admin: reject event
    NgoEventResponse rejectEvent(Long eventId, Long adminUserId, String reason);

    // Admin: get all events (all statuses)
    List<NgoEventResponse> getAllEventsForAdmin();

    // Get event participants (NGO sees own events only, admin sees all)
    List<NgoParticipantResponse> getEventParticipants(Long userId, Long eventId, boolean isAdmin);
}
