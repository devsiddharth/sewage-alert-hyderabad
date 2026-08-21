package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * EventData: Lightweight projection of event data for AI context building.
 */
@Data
public class EventData {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String eventDate;
    private String organizerName;
    private Integer capacity;
    private int registeredCount;
}
