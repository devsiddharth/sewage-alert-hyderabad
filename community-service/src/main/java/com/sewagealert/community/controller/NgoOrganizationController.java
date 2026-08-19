package com.sewagealert.community.controller;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.model.NgoApplicationStatus;
import com.sewagealert.community.service.NgoOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NgoController: NGO application, verification, dashboard, and admin management APIs.
 */
@RestController
@RequestMapping("/api/v1/ngo")
@Tag(name = "NGO Organization", description = "NGO application, verification, profile and dashboard APIs")
public class NgoOrganizationController {

    private final NgoOrganizationService ngoService;

    public NgoOrganizationController(NgoOrganizationService ngoService) {
        this.ngoService = ngoService;
    }

    // ---- NGO Representative endpoints ----

    @PostMapping("/apply")
    @Operation(summary = "Submit NGO application", description = "Any authenticated user can apply to become a verified NGO.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Application submitted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate application")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> apply(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody NgoApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted", ngoService.submitApplication(userId, request)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my NGO organization")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> getMyOrganization(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Organization retrieved", ngoService.getMyOrganization(userId)));
    }

    @PutMapping("/my")
    @Operation(summary = "Update my NGO profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> updateProfile(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody NgoApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", ngoService.updateProfile(userId, request)));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get NGO dashboard overview")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoDashboardResponse>> getDashboard(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved", ngoService.getDashboard(userId)));
    }

    // ---- Admin endpoints ----

    @GetMapping("/admin/all")
    @Operation(summary = "List all NGO applications (admin)", description = "Returns all NGO applications, optionally filtered by status.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoOrganizationResponse>>> getAllApplications(
            @Parameter(description = "Optional status filter") @RequestParam(required = false) NgoApplicationStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved", ngoService.getAllApplications(status)));
    }

    @GetMapping("/admin/{ngoId}")
    @Operation(summary = "Get NGO application details (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> getApplication(
            @PathVariable Long ngoId) {
        return ResponseEntity.ok(ApiResponse.success("Application retrieved", ngoService.getApplicationById(ngoId)));
    }

    @PostMapping("/admin/{ngoId}/approve")
    @Operation(summary = "Approve NGO application (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> approveNgo(
            @PathVariable Long ngoId,
            @RequestHeader("X-Auth-User-Id") Long adminUserId) {
        return ResponseEntity.ok(ApiResponse.success("NGO approved", ngoService.approveNgo(ngoId, adminUserId)));
    }

    @PostMapping("/admin/{ngoId}/reject")
    @Operation(summary = "Reject NGO application (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> rejectNgo(
            @PathVariable Long ngoId,
            @RequestHeader("X-Auth-User-Id") Long adminUserId,
            @RequestBody NgoApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("NGO rejected",
                ngoService.rejectNgo(ngoId, adminUserId, request.getRejectionReason())));
    }

    @PostMapping("/admin/{ngoId}/suspend")
    @Operation(summary = "Suspend an NGO (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> suspendNgo(
            @PathVariable Long ngoId,
            @RequestHeader("X-Auth-User-Id") Long adminUserId,
            @RequestBody NgoApprovalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("NGO suspended",
                ngoService.suspendNgo(ngoId, adminUserId, request.getRejectionReason())));
    }

    @PostMapping("/admin/{ngoId}/reactivate")
    @Operation(summary = "Reactivate a suspended NGO (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoOrganizationResponse>> reactivateNgo(
            @PathVariable Long ngoId,
            @RequestHeader("X-Auth-User-Id") Long adminUserId) {
        return ResponseEntity.ok(ApiResponse.success("NGO reactivated",
                ngoService.reactivateNgo(ngoId, adminUserId)));
    }
}
