package com.sewagealert.community.controller;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.service.NgoDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ngo/drives")
@Tag(name = "NGO Drives", description = "NGO drive management, participation and progress tracking")
public class NgoDriveController {

    private final NgoDriveService driveService;

    public NgoDriveController(NgoDriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping
    @Operation(summary = "Create a drive")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoDriveResponse>> createDrive(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody NgoDriveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Drive created", driveService.createDrive(userId, request)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my drives")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoDriveResponse>>> getMyDrives(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Drives retrieved", driveService.getMyDrives(userId)));
    }

    @PutMapping("/{driveId}")
    @Operation(summary = "Update a drive")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoDriveResponse>> updateDrive(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long driveId,
            @Valid @RequestBody NgoDriveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Drive updated", driveService.updateDrive(userId, driveId, request)));
    }

    @DeleteMapping("/{driveId}")
    @Operation(summary = "Delete a drive")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteDrive(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long driveId) {
        driveService.deleteDrive(userId, driveId);
        return ResponseEntity.ok(ApiResponse.success("Drive deleted", null));
    }

    @GetMapping("/{driveId}")
    @Operation(summary = "Get drive details")
    public ResponseEntity<ApiResponse<NgoDriveResponse>> getDrive(@PathVariable Long driveId) {
        return ResponseEntity.ok(ApiResponse.success("Drive retrieved", driveService.getDrive(driveId)));
    }

    @PostMapping("/{driveId}/participate")
    @Operation(summary = "Join a drive")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> participate(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long driveId) {
        driveService.participateInDrive(userId, driveId);
        return ResponseEntity.ok(ApiResponse.success("Joined drive", null));
    }

    @PostMapping("/{driveId}/cancel")
    @Operation(summary = "Cancel drive participation")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> cancelParticipation(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long driveId) {
        driveService.cancelDriveParticipation(userId, driveId);
        return ResponseEntity.ok(ApiResponse.success("Participation cancelled", null));
    }

    @GetMapping("/{driveId}/participants")
    @Operation(summary = "Get drive participants")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoParticipantResponse>>> getParticipants(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long driveId,
            @Parameter(description = "Is admin?") @RequestParam(defaultValue = "false") boolean isAdmin) {
        return ResponseEntity.ok(ApiResponse.success("Participants retrieved",
                driveService.getDriveParticipants(userId, driveId, isAdmin)));
    }

    @PutMapping("/{driveId}/progress")
    @Operation(summary = "Update drive progress")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoDriveResponse>> updateProgress(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long driveId,
            @RequestParam String progressNotes,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success("Progress updated",
                driveService.updateDriveProgress(userId, driveId, progressNotes, status)));
    }
}
