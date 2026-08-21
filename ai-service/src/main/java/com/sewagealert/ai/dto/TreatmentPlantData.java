package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * TreatmentPlantData: Lightweight projection of treatment plant data for AI context building.
 */
@Data
public class TreatmentPlantData {
    private Long id;
    private String name;
    private Double capacityMld;
    private String location;
    private String treatmentMethod;
    private String waterReuseInfo;
    private String description;
}
