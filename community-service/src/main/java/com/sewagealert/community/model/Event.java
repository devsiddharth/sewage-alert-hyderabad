package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "events")
// Event: Represents an awareness event organized by authorities or NGOs — citizens can register to attend
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;  // Physical venue or online meeting link

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;  // Date when the event takes place

    @Column(name = "organizer_name", nullable = false)
    private String organizerName;  // Authority department or NGO name organizing the event

    // organizerId: References the authority user who created this event (from auth-service)
    @Column(name = "organizer_id")
    private Long organizerId;

    // capacity: Maximum number of attendees — null means unlimited
    private Integer capacity;

    // registrations: Citizens who registered for this event — cascade ALL so deleting event removes registrations
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    // Helper: Adds a registration and maintains bidirectional relationship
    public void addRegistration(EventRegistration registration) {
        registrations.add(registration);
        registration.setEvent(this);
    }

}
