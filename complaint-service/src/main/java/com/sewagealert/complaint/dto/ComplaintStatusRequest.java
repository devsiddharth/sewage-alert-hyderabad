package com.sewagealert.complaint.dto;

import com.sewagealert.complaint.model.ComplaintPriority;
import com.sewagealert.complaint.model.ComplaintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// ComplaintStatusRequest: DTO used by authorities to update the status and priority of a complaint
@Schema(description = "Status/priority update for a complaint — used by authorities and field officers")
public class ComplaintStatusRequest {

    @Schema(description = "New status to set", example = "IN_PROGRESS")
    @NotNull(message = "Status is required")
    private ComplaintStatus status;  // New status to set (e.g., IN_PROGRESS, RESOLVED)

    @Schema(description = "Optional priority assignment", example = "HIGH")
    private ComplaintPriority priority;  // Optional priority assignment (e.g., HIGH, CRITICAL)

    @Schema(description = "Optional explanation for the status change", example = "Team dispatched to inspect the blockage")
    private String remarks;  // Optional explanation for the status change

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
