package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Sewage Treatment Plant (STP) create/update request")
public class TreatmentPlantRequest {

    @Schema(description = "Plant name", example = "Amberpet STP")
    @NotBlank(message = "Plant name is required")
    private String name;

    @Schema(description = "Treatment capacity in MLD", example = "172.0")
    @NotNull(message = "Capacity is required")
    private Double capacityMld;

    @Schema(description = "Plant location", example = "Amberpet, Hyderabad")
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(description = "Treatment method", example = "Activated Sludge Process")
    private String treatmentMethod;

    @Schema(description = "Water reuse information")
    private String waterReuseInfo;

    @Schema(description = "Plant description")
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
