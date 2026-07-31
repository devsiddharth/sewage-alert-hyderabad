package com.sewagealert.complaint.dto;

import com.sewagealert.complaint.model.ComplaintPriority;
import com.sewagealert.complaint.model.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

// ComplaintStatusRequest: DTO used by authorities to update the status and priority of a complaint
public class ComplaintStatusRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;  // New status to set (e.g., IN_PROGRESS, RESOLVED)

    private ComplaintPriority priority;  // Optional priority assignment (e.g., HIGH, CRITICAL)

    private String remarks;  // Optional explanation for the status change

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
