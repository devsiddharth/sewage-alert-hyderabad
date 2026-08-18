package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// EventRequest: DTO for creating or updating an awareness event
@Schema(description = "Awareness event create/update request")
public class EventRequest {

    @Schema(description = "Event title", example = "Lake Restoration Awareness Walk")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Event description", example = "Guided walk around Hussain Sagar to raise awareness")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Venue / location", example = "Necklace Road, Hyderabad")
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Event date", example = "2026-09-15")
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @Schema(description = "Organizer name", example = "Green Hyderabad Trust")
    @NotBlank(message = "Organizer name is required")
    private String organizerName;

    @Schema(description = "Optional registration capacity", example = "100")
    private Integer capacity;

    // Getters and Setters
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

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
