package com.sewagealert.community.dto;

import com.sewagealert.community.model.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDate eventDate;
    private String organizerName;
    private Long organizerId;
    private Integer capacity;
    private int registeredCount;  // Computed field: number of registered citizens
    private LocalDateTime createdAt;

    public EventResponse() {}

    public static EventResponse fromEntity(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setLocation(event.getLocation());
        response.setEventDate(event.getEventDate());
        response.setOrganizerName(event.getOrganizerName());
        response.setOrganizerId(event.getOrganizerId());
        response.setCapacity(event.getCapacity());
        response.setRegisteredCount(event.getRegistrations() != null ? event.getRegistrations().size() : 0);
        response.setCreatedAt(event.getCreatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
