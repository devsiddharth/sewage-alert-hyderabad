package com.sewagealert.community.dto;

import com.sewagealert.community.model.TreatmentPlant;
import java.time.LocalDateTime;

public class TreatmentPlantResponse {

    private Long id;
    private String name;
    private Double capacityMld;
    private String location;
    private String treatmentMethod;
    private String waterReuseInfo;
    private String description;
    private LocalDateTime createdAt;

    public TreatmentPlantResponse() {}

    public static TreatmentPlantResponse fromEntity(TreatmentPlant plant) {
        TreatmentPlantResponse response = new TreatmentPlantResponse();
        response.setId(plant.getId());
        response.setName(plant.getName());
        response.setCapacityMld(plant.getCapacityMld());
        response.setLocation(plant.getLocation());
        response.setTreatmentMethod(plant.getTreatmentMethod());
        response.setWaterReuseInfo(plant.getWaterReuseInfo());
        response.setDescription(plant.getDescription());
        response.setCreatedAt(plant.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
