package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.AssignComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AdminComplaintController: Admin-only complaint management endpoints.
// Role verification (ADMIN) happens in the service layer against AUTH-SERVICE — the
// frontend can never bypass it by hiding a button.
@RestController
@RequestMapping("/api/v1/complaints/admin")
public class AdminComplaintController {

    private final ComplaintService complaintService;

    public AdminComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PutMapping("/{id}/assign")
    // PUT /api/v1/complaints/admin/{id}/assign: Assigns (or reassigns) a complaint to a
    // field officer. The acting admin's id comes from the gateway header — never the body.
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignComplaint(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long adminUserId,
            @Valid @RequestBody AssignComplaintRequest request) {
        ComplaintResponse response = complaintService.assignComplaint(id, request.getFieldOfficerId(), adminUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint assigned successfully", response));
    }
}
