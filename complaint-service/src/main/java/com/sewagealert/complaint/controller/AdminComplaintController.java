package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.AssignComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AdminComplaintController: Admin-only complaint management endpoints.
// Role verification (ADMIN) happens in the service layer against AUTH-SERVICE — the
// frontend can never bypass it by hiding a button.
@RestController
@RequestMapping("/api/v1/complaints/admin")
@Tag(name = "Complaints (Admin)", description = "Admin-only complaint management — role verification (ADMIN) happens in the service layer")
public class AdminComplaintController {

    private final ComplaintService complaintService;

    public AdminComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PutMapping("/{id}/assign")
    @Operation(
            summary = "Assign a complaint to a field officer (admin)",
            description = "Assigns (or reassigns) a complaint to a field officer. The acting admin's id comes "
                    + "from the X-Auth-User-Id header — never the body — and the ADMIN role is verified server-side."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid assignment (e.g. officer not found / not a field officer)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not an ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    // PUT /api/v1/complaints/admin/{id}/assign: Assigns (or reassigns) a complaint to a
    // field officer. The acting admin's id comes from the gateway header — never the body.
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignComplaint(
            @Parameter(description = "Complaint id", example = "1") @PathVariable Long id,
            @Parameter(description = "Authenticated admin's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long adminUserId,
            @Valid @RequestBody AssignComplaintRequest request) {
        ComplaintResponse response = complaintService.assignComplaint(id, request.getFieldOfficerId(), adminUserId);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint assigned successfully", response));
    }
}
