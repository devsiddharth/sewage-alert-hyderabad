package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// FieldOfficerComplaintController: Endpoints for field officers. The officer is always
// derived from the authenticated gateway header (X-Auth-User-Id) — an arbitrary officer id
// from the query string/body is never trusted. Role + ownership checks live in the service.
@RestController
@RequestMapping("/api/v1/complaints/field-officer")
public class FieldOfficerComplaintController {

    private final ComplaintService complaintService;

    public FieldOfficerComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    // GET /api/v1/complaints/field-officer: All complaints assigned to the logged-in officer
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAssignedComplaints(
            @RequestHeader("X-Auth-User-Id") Long officerUserId) {
        List<ComplaintResponse> responses = complaintService.getAssignedComplaints(officerUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Assigned complaints retrieved successfully", responses));
    }

    @PatchMapping("/{id}/status")
    // PATCH /api/v1/complaints/field-officer/{id}/status: Status update for a complaint
    // assigned to the logged-in officer (ownership enforced server-side).
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long officerUserId,
            @Valid @RequestBody ComplaintStatusRequest request) {
        ComplaintResponse response = complaintService.updateAssignedComplaintStatus(id, officerUserId, request);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint status updated successfully", response));
    }
}
