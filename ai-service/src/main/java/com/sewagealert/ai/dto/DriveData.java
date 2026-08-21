package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * DriveData: Lightweight projection of drive data for AI context building.
 */
@Data
public class DriveData {
    private Long id;
    private String title;
    private String description;
    private String driveType;
    private String location;
    private String startDate;
    private String endDate;
    private String status;
    private String ngoOrganizationName;
    private long currentParticipants;
    private Integer totalTarget;
}
