package com.sewagealert.community.repository;

import com.sewagealert.community.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // findByEventId: Returns all registrations for a specific event
    List<EventRegistration> findByEventId(Long eventId);

    // findByUserId: Returns all events a specific user has registered for
    List<EventRegistration> findByUserId(Long userId);

    // findByEventIdAndUserId: Checks if a user is already registered for an event (prevents duplicate registration)
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
}
