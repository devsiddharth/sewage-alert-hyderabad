package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * PipelineData: Lightweight projection of pipeline infrastructure data for AI context building.
 */
@Data
public class PipelineData {
    private Long id;
    private String locality;
    private Integer installationYear;
    private Integer designedCapacity;
    private String maintenanceDate;
    private String operationalStatus;
    private String notes;
}
