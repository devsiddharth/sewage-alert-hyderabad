package com.sewagealert.community.dto;

import com.sewagealert.community.model.Pipeline;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class PipelineRequest {

    @NotBlank(message = "Locality is required")
    private String locality;

    private Integer installationYear;
    private Integer designedCapacity;
    private LocalDate maintenanceDate;

    @NotNull(message = "Operational status is required")
    private Pipeline.OperationalStatus operationalStatus;

    private String notes;

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public Integer getInstallationYear() { return installationYear; }
    public void setInstallationYear(Integer installationYear) { this.installationYear = installationYear; }

    public Integer getDesignedCapacity() { return designedCapacity; }
    public void setDesignedCapacity(Integer designedCapacity) { this.designedCapacity = designedCapacity; }

    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }

    public Pipeline.OperationalStatus getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(Pipeline.OperationalStatus operationalStatus) { this.operationalStatus = operationalStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
