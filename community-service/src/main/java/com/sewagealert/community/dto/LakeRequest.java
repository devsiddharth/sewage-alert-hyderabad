package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Lake information create/update request")
public class LakeRequest {

    @Schema(description = "Lake name", example = "Hussain Sagar")
    @NotBlank(message = "Lake name is required")
    private String name;

    @Schema(description = "Location", example = "Hyderabad")
    private String location;

    @Schema(description = "Restoration status", example = "IN_PROGRESS")
    private String restorationStatus;

    @Schema(description = "Water source", example = "Musi River")
    private String waterSource;

    @Schema(description = "Id of the connected treatment plant", example = "3")
    private Long connectedStpId;

    @Schema(description = "Environmental updates")
    private String environmentalUpdates;

    @Schema(description = "Lake description")
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getRestorationStatus() { return restorationStatus; }
    public void setRestorationStatus(String restorationStatus) { this.restorationStatus = restorationStatus; }

    public String getWaterSource() { return waterSource; }
    public void setWaterSource(String waterSource) { this.waterSource = waterSource; }

    public Long getConnectedStpId() { return connectedStpId; }
    public void setConnectedStpId(Long connectedStpId) { this.connectedStpId = connectedStpId; }

    public String getEnvironmentalUpdates() { return environmentalUpdates; }
    public void setEnvironmentalUpdates(String environmentalUpdates) { this.environmentalUpdates = environmentalUpdates; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
