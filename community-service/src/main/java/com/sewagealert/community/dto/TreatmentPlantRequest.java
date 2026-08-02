package com.sewagealert.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TreatmentPlantRequest {

    @NotBlank(message = "Plant name is required")
    private String name;

    @NotNull(message = "Capacity is required")
    private Double capacityMld;

    @NotBlank(message = "Location is required")
    private String location;

    private String treatmentMethod;
    private String waterReuseInfo;
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getCapacityMld() { return capacityMld; }
    public void setCapacityMld(Double capacityMld) { this.capacityMld = capacityMld; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTreatmentMethod() { return treatmentMethod; }
    public void setTreatmentMethod(String treatmentMethod) { this.treatmentMethod = treatmentMethod; }

    public String getWaterReuseInfo() { return waterReuseInfo; }
    public void setWaterReuseInfo(String waterReuseInfo) { this.waterReuseInfo = waterReuseInfo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
