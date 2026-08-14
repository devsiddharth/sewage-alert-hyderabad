package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.LakeRequest;
import com.sewagealert.community.dto.LakeResponse;
import com.sewagealert.community.dto.external.overpass.LakeGeoData;
import com.sewagealert.community.service.LakeExternalService;
import com.sewagealert.community.service.LakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lakes")
@Tag(name = "Lakes", description = "Lake information and restoration status — maintained by authorities")
// LakeController: Manages lake information and restoration status — maintained by authorities
public class LakeController {

    private final LakeService lakeService;
    private final LakeExternalService lakeExternalService;

    public LakeController(LakeService lakeService, LakeExternalService lakeExternalService) {
        this.lakeService = lakeService;
        this.lakeExternalService = lakeExternalService;
    }

    @PostMapping
    @Operation(
            summary = "Create a lake record",
            description = "Creates a lake information record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lake created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<LakeResponse>> createLake(@Valid @RequestBody LakeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lake created successfully", lakeService.createLake(request)));
    }

    @GetMapping
    @Operation(
            summary = "List all lakes",
            description = "Returns all lake records."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lakes retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<LakeResponse>>> getAllLakes() {
        return ResponseEntity.ok(ApiResponse.success("Lakes retrieved successfully", lakeService.getAllLakes()));
    }

    // getExternalLakes: Retrieves lake data from the external OpenStreetMap Overpass API
    @GetMapping("/external")
    @Operation(
            summary = "Fetch lakes from OpenStreetMap (Overpass API)",
            description = "Returns lake geo-data retrieved from the external OpenStreetMap Overpass API."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lakes retrieved from OpenStreetMap successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "External service error")
    })
    public ResponseEntity<ApiResponse<List<LakeGeoData>>> getExternalLakes() {
        return ResponseEntity.ok(ApiResponse.success("Lakes retrieved from OpenStreetMap successfully",
                lakeExternalService.getLakes()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a lake by id",
            description = "Returns a single lake record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lake retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lake not found")
    })
    public ResponseEntity<ApiResponse<LakeResponse>> getLake(
            @Parameter(description = "Lake id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lake retrieved successfully", lakeService.getLake(id)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a lake record",
            description = "Updates an existing lake record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lake updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lake not found")
    })
    public ResponseEntity<ApiResponse<LakeResponse>> updateLake(
            @Parameter(description = "Lake id", example = "1") @PathVariable Long id, @Valid @RequestBody LakeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Lake updated successfully", lakeService.updateLake(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a lake record",
            description = "Deletes an existing lake record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lake deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lake not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteLake(
            @Parameter(description = "Lake id", example = "1") @PathVariable Long id) {
        lakeService.deleteLake(id);
        return ResponseEntity.ok(ApiResponse.success("Lake deleted successfully", null));
    }
}
