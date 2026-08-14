package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// FieldOfficerComplaintController: Endpoints for field officers. The officer is always
// derived from the authenticated gateway header (X-Auth-User-Id) — an arbitrary officer id
// from the query string/body is never trusted. Role + ownership checks live in the service.
@RestController
@RequestMapping("/api/v1/complaints/field-officer")
@Tag(name = "Complaints (Field Officer)", description = "Field-officer complaint workflow — the officer is always derived from the authenticated gateway header")
public class FieldOfficerComplaintController {

    private final ComplaintService complaintService;

    public FieldOfficerComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    @Operation(
            summary = "List complaints assigned to the logged-in officer",
            description = "Returns all complaints assigned to the authenticated field officer (derived from the "
                    + "X-Auth-User-Id header)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assigned complaints retrieved successfully")
    })
    @SecurityRequirement(name = "bearerAuth")
    // GET /api/v1/complaints/field-officer: All complaints assigned to the logged-in officer
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAssignedComplaints(
            @Parameter(description = "Authenticated officer's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long officerUserId) {
        List<ComplaintResponse> responses = complaintService.getAssignedComplaints(officerUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Assigned complaints retrieved successfully", responses));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update status of an assigned complaint",
            description = "Updates the status of a complaint assigned to the logged-in officer. Ownership is "
                    + "enforced server-side using the X-Auth-User-Id header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or invalid status transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Complaint is not assigned to this officer"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    // PATCH /api/v1/complaints/field-officer/{id}/status: Status update for a complaint
    // assigned to the logged-in officer (ownership enforced server-side).
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @Parameter(description = "Complaint id", example = "1") @PathVariable Long id,
            @Parameter(description = "Authenticated officer's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long officerUserId,
            @Valid @RequestBody ComplaintStatusRequest request) {
        ComplaintResponse response = complaintService.updateAssignedComplaintStatus(id, officerUserId, request);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint status updated successfully", response));
    }
}
