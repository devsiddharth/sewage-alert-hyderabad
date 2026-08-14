package com.sewagealert.complaint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

// AssignComplaintRequest: Body for the admin complaint-assignment endpoint. Only the
// target field officer's user id (auth-service id) is needed — everything else is derived
// server-side from the authenticated caller and the complaint.
@Schema(description = "Admin complaint-assignment request — only the target field officer's user id is needed")
public class AssignComplaintRequest {

    @Schema(description = "Auth-service user id of the field officer to assign the complaint to", example = "5")
    @NotNull(message = "Field officer is required")
    private Long fieldOfficerId;

    public Long getFieldOfficerId() { return fieldOfficerId; }
    public void setFieldOfficerId(Long fieldOfficerId) { this.fieldOfficerId = fieldOfficerId; }
}
