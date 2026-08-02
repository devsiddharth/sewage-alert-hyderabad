package com.sewagealert.community.dto;

import com.sewagealert.community.model.Lake;
import java.time.LocalDateTime;

public class LakeResponse {

    private Long id;
    private String name;
    private String location;
    private String restorationStatus;
    private String waterSource;
    private Long connectedStpId;
    private String environmentalUpdates;
    private String description;
    private LocalDateTime createdAt;

    public LakeResponse() {}

    public static LakeResponse fromEntity(Lake lake) {
        LakeResponse response = new LakeResponse();
        response.setId(lake.getId());
        response.setName(lake.getName());
        response.setLocation(lake.getLocation());
        response.setRestorationStatus(lake.getRestorationStatus());
        response.setWaterSource(lake.getWaterSource());
        response.setConnectedStpId(lake.getConnectedStpId());
        response.setEnvironmentalUpdates(lake.getEnvironmentalUpdates());
        response.setDescription(lake.getDescription());
        response.setCreatedAt(lake.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
