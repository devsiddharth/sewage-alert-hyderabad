package com.sewagealert.community.dto;

import com.sewagealert.community.model.Pipeline;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PipelineResponse {

    private Long id;
    private String locality;
    private Integer installationYear;
    private Integer designedCapacity;
    private LocalDate maintenanceDate;
    private String operationalStatus;
    private String notes;
    private LocalDateTime createdAt;

    public PipelineResponse() {}

    public static PipelineResponse fromEntity(Pipeline pipeline) {
        PipelineResponse response = new PipelineResponse();
        response.setId(pipeline.getId());
        response.setLocality(pipeline.getLocality());
        response.setInstallationYear(pipeline.getInstallationYear());
        response.setDesignedCapacity(pipeline.getDesignedCapacity());
        response.setMaintenanceDate(pipeline.getMaintenanceDate());
        response.setOperationalStatus(pipeline.getOperationalStatus().name());
        response.setNotes(pipeline.getNotes());
        response.setCreatedAt(pipeline.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public Integer getInstallationYear() { return installationYear; }
    public void setInstallationYear(Integer installationYear) { this.installationYear = installationYear; }

    public Integer getDesignedCapacity() { return designedCapacity; }
    public void setDesignedCapacity(Integer designedCapacity) { this.designedCapacity = designedCapacity; }

    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }

    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
