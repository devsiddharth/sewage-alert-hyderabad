package com.sewagealert.complaint.dto;

import jakarta.validation.constraints.NotNull;

// AssignComplaintRequest: Body for the admin complaint-assignment endpoint. Only the
// target field officer's user id (auth-service id) is needed — everything else is derived
// server-side from the authenticated caller and the complaint.
public class AssignComplaintRequest {

    @NotNull(message = "Field officer is required")
    private Long fieldOfficerId;

    public Long getFieldOfficerId() { return fieldOfficerId; }
    public void setFieldOfficerId(Long fieldOfficerId) { this.fieldOfficerId = fieldOfficerId; }
}
