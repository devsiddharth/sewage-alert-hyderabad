package com.sewagealert.community.dto;

import com.sewagealert.community.model.Pipeline;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Sewage pipeline infrastructure create/update request")
public class PipelineRequest {

    @Schema(description = "Locality served by the pipeline", example = "Banjara Hills")
    @NotBlank(message = "Locality is required")
    private String locality;

    @Schema(description = "Year of installation", example = "2015")
    private Integer installationYear;

    @Schema(description = "Designed capacity (MLD)", example = "50")
    private Integer designedCapacity;

    @Schema(description = "Last maintenance date", example = "2026-03-10")
    private LocalDate maintenanceDate;

    @Schema(description = "Operational status", example = "OPERATIONAL")
    @NotNull(message = "Operational status is required")
    private Pipeline.OperationalStatus operationalStatus;

    @Schema(description = "Additional notes")
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
