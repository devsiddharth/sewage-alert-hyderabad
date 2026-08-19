package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Admin event approval/rejection request")
public class EventApprovalRequest {

    @Schema(description = "Rejection reason (required when rejecting)")
    private String rejectionReason;

    @Schema(description = "True to approve, false to reject")
    @NotNull(message = "Approval decision is required")
    private boolean approved;
}
