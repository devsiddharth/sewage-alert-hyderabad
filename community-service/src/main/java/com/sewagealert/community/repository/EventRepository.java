package com.sewagealert.community.repository;

import com.sewagealert.community.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // findByEventDateAfter: Finds all upcoming events (events with a future date)
    List<Event> findByEventDateAfter(LocalDate date);

    // findByEventDateBefore: Finds all past events
    List<Event> findByEventDateBefore(LocalDate date);

    // findByOrganizerId: Finds all events created by a specific authority
    List<Event> findByOrganizerId(Long organizerId);
}
