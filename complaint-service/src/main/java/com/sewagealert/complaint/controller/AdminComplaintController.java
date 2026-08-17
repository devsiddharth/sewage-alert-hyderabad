package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.AssignComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.model.ComplaintPriority;
import com.sewagealert.complaint.model.ComplaintStatus;
import com.sewagealert.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/{id}/resolve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Resolve a complaint with proof photo (admin)",
            description = "Marks a complaint as RESOLVED. A resolution-proof photo is MANDATORY: the "
                    + "multipart request must include a 'proofImage' file part (JPG/PNG/WEBP, max 10 MB). "
                    + "The photo is validated and uploaded to object storage BEFORE the status change, so a "
                    + "failed upload never resolves the complaint. The acting admin's id comes from the "
                    + "X-Auth-User-Id header — never the body — and the ADMIN role is verified server-side."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint resolved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing/invalid proof photo, or invalid remarks"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Authenticated user is not an ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    // POST /api/v1/complaints/admin/{id}/resolve: Resolves a complaint with a mandatory proof
    // photo. The image is uploaded to object storage first; only then is the complaint marked
    // RESOLVED with the returned proof URL persisted on the complaint row.
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolveComplaint(
            @Parameter(description = "Complaint id", example = "1") @PathVariable Long id,
            @Parameter(description = "Authenticated admin's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long adminUserId,
            @Parameter(description = "Optional resolution remarks")
            @RequestParam(value = "remarks", required = false) String remarks,
            @Parameter(description = "Optional priority assignment", example = "HIGH")
            @RequestParam(value = "priority", required = false) ComplaintPriority priority,
            @Parameter(description = "Mandatory resolution-proof photo (JPG/PNG/WEBP, max 10 MB)")
            @RequestPart(value = "proofImage", required = false) MultipartFile proofImage) {

        ComplaintStatusRequest request = new ComplaintStatusRequest();
        request.setStatus(ComplaintStatus.RESOLVED);
        request.setPriority(priority);
        request.setRemarks(remarks);

        ComplaintResponse response = complaintService.resolveComplaint(id, adminUserId, request, proofImage);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint resolved successfully", response));
    }
}
