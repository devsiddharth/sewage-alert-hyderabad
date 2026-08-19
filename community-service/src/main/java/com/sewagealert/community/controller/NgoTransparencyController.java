package com.sewagealert.community.controller;

import com.sewagealert.community.dto.*;
import com.sewagealert.community.service.NgoTransparencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ngo/transparency")
@Tag(name = "NGO Transparency", description = "Achievements, progress, funding and expense transparency")
public class NgoTransparencyController {

    private final NgoTransparencyService transparencyService;

    public NgoTransparencyController(NgoTransparencyService transparencyService) {
        this.transparencyService = transparencyService;
    }

    // ---- Achievements ----

    @PostMapping("/achievements")
    @Operation(summary = "Create an achievement")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoAchievementResponse>> createAchievement(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody NgoAchievementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Achievement created", transparencyService.createAchievement(userId, request)));
    }

    @GetMapping("/achievements")
    @Operation(summary = "Get my achievements")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoAchievementResponse>>> getAchievements(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Achievements retrieved", transparencyService.getMyAchievements(userId)));
    }

    @PutMapping("/achievements/{achievementId}")
    @Operation(summary = "Update an achievement")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoAchievementResponse>> updateAchievement(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long achievementId,
            @Valid @RequestBody NgoAchievementRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Achievement updated",
                transparencyService.updateAchievement(userId, achievementId, request)));
    }

    @DeleteMapping("/achievements/{achievementId}")
    @Operation(summary = "Delete an achievement")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteAchievement(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long achievementId) {
        transparencyService.deleteAchievement(userId, achievementId);
        return ResponseEntity.ok(ApiResponse.success("Achievement deleted", null));
    }

    // ---- Progress ----

    @GetMapping("/progress")
    @Operation(summary = "Get my NGO progress metrics")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoProgressResponse>> getProgress(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Progress retrieved", transparencyService.getMyProgress(userId)));
    }

    // ---- Funds ----

    @PostMapping("/funds")
    @Operation(summary = "Create a fund record")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoFundResponse>> createFund(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody NgoFundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fund record created", transparencyService.createFundRecord(userId, request)));
    }

    @GetMapping("/funds")
    @Operation(summary = "Get my fund records")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoFundResponse>>> getFunds(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Fund records retrieved", transparencyService.getMyFunds(userId)));
    }

    @PutMapping("/funds/{fundId}")
    @Operation(summary = "Update a fund record")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoFundResponse>> updateFund(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long fundId,
            @Valid @RequestBody NgoFundRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fund record updated",
                transparencyService.updateFundRecord(userId, fundId, request)));
    }

    // ---- Expenses ----

    @PostMapping("/expenses")
    @Operation(summary = "Create an expense record", description = "Validates that expenses don't exceed allocated funds.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<NgoExpenseResponse>> createExpense(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody NgoExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense recorded", transparencyService.createExpenseRecord(userId, request)));
    }

    @GetMapping("/expenses")
    @Operation(summary = "Get my expense records")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<NgoExpenseResponse>>> getExpenses(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Expense records retrieved", transparencyService.getMyExpenses(userId)));
    }
}
