package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * NgoEventData: Lightweight projection of NGO event data for AI context building.
 */
@Data
public class NgoEventData {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String eventDate;
    private String ngoOrganizationName;
    private String approvalStatus;
    private int registeredCount;
}
