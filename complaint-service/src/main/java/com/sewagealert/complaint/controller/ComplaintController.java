package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.ComplaintRequest;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.dto.ComplaintStatusRequest;
import com.sewagealert.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
// ComplaintController: REST controller exposing complaint management endpoints
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // POST /api/v1/complaints: Creates a new complaint — multipart/form-data. Form fields
    // (title, description, latitude, longitude) bind to ComplaintRequest; optional "images"
    // file parts are uploaded to object storage and only URLs are persisted.
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @ModelAttribute ComplaintRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        ComplaintResponse response = complaintService.createComplaint(userId, request, images);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint created successfully", response));
    }

    @GetMapping
    // GET /api/v1/complaints: Lists all complaints (for admin/authority) or filters by user if userId param provided
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaints(
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
    // GET /api/v1/complaints/{id}: Retrieves a single complaint with full details including images and history
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaint(@PathVariable Long id) {
        ComplaintResponse response = complaintService.getComplaint(id);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint retrieved successfully", response));
    }

    @PatchMapping("/{id}/status")
    // PATCH /api/v1/complaints/{id}/status: Updates complaint status and priority — used by authorities
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @PathVariable Long id,
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody ComplaintStatusRequest request) {
        ComplaintResponse response = complaintService.updateStatus(id, userId, request);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    // DELETE /api/v1/complaints/{id}: Deletes a complaint and all associated data
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity
                .ok(ApiResponse.success("Complaint deleted successfully", null));
    }
}
