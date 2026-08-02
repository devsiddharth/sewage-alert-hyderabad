package com.sewagealert.community.dto;

import jakarta.validation.constraints.NotBlank;

public class LakeRequest {

    @NotBlank(message = "Lake name is required")
    private String name;

    private String location;
    private String restorationStatus;
    private String waterSource;
    private Long connectedStpId;
    private String environmentalUpdates;
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
