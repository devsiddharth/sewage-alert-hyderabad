package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * LakeData: Lightweight projection of lake data for AI context building.
 */
@Data
public class LakeData {
    private Long id;
    private String name;
    private String location;
    private String restorationStatus;
    private String waterSource;
    private Long connectedStpId;
    private String environmentalUpdates;
    private String description;
}
