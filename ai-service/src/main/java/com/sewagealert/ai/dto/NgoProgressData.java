package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * NgoProgressData: Lightweight projection of NGO progress data for AI context building.
 */
@Data
public class NgoProgressData {
    private Long ngoOrganizationId;
    private int complaintsAddressed;
    private int areasCovered;
    private int drivesConducted;
    private int eventsConducted;
    private int volunteersInvolved;
    private int peopleReached;
}
