package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.ComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
@Tag(name = "Complaints", description = "Citizen complaint creation, listing and status tracking")
// ComplaintController: REST controller exposing complaint management endpoints
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new complaint",
            description = "Creates a complaint as multipart/form-data: form fields (title, description, "
                    + "latitude, longitude) plus optional image file parts (max 10 MB each), which are "
                    + "uploaded to object storage. Only the returned image URLs are persisted."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Complaint created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or invalid image"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "Image(s) exceed the maximum upload size")
    })
    @SecurityRequirement(name = "bearerAuth")
    // POST /api/v1/complaints: Creates a new complaint — multipart/form-data. Form fields
    // (title, description, latitude, longitude) bind to ComplaintRequest; optional "images"
    // file parts are uploaded to object storage and only URLs are persisted.
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @ModelAttribute ComplaintRequest request,
            @Parameter(description = "Optional complaint images (multipart file parts)")
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        ComplaintResponse response = complaintService.createComplaint(userId, request, images);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint created successfully", response));
    }

    @GetMapping
    @Operation(
            summary = "List complaints",
            description = "Lists all complaints, or filters by the reporting user when the optional userId "
                    + "parameter is provided."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaints retrieved successfully")
    })
    // GET /api/v1/complaints: Lists all complaints (for admin/authority) or filters by user if userId param provided
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaints(
            @Parameter(description = "Optional filter — auth-service id of the reporting user", example = "1")
            @RequestParam(required = false) Long userId) {
        List<ComplaintResponse> responses;
        if (userId != null) {
            responses = complaintService.getComplaintsByUser(userId);
        } else {
            responses = complaintService.getAllComplaints();
        }
        return ResponseEntity
                .ok(ApiResponse.success("Complaints retrieved successfully", responses));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a complaint by id",
            description = "Retrieves a single complaint with full details, including image URLs and status history."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    // GET /api/v1/complaints/{id}: Retrieves a single complaint with full details including images and history
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaint(
            @Parameter(description = "Complaint id", example = "1") @PathVariable Long id) {
        ComplaintResponse response = complaintService.getComplaint(id);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint retrieved successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update complaint status (authority)",
            description = "Updates a complaint's status and priority. The acting user id comes from the "
                    + "X-Auth-User-Id header."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or invalid status transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    // PATCH /api/v1/complaints/{id}/status: Updates complaint status and priority — used by authorities
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @Parameter(description = "Complaint id", example = "1") @PathVariable Long id,
            @Parameter(description = "Authenticated user's auth-service id (set by the gateway)", example = "1")
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody ComplaintStatusRequest request) {
        ComplaintResponse response = complaintService.updateStatus(id, userId, request);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a complaint",
            description = "Deletes a complaint and all associated data (history, images)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    // DELETE /api/v1/complaints/{id}: Deletes a complaint and all associated data
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(
            @Parameter(description = "Complaint id", example = "1") @PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint deleted successfully", null));
    }
}
