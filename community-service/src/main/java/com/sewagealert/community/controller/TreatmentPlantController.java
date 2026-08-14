package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.TreatmentPlantRequest;
import com.sewagealert.community.dto.TreatmentPlantResponse;
import com.sewagealert.community.dto.external.telangana.StpLocationData;
import com.sewagealert.community.service.TreatmentPlantExternalService;
import com.sewagealert.community.service.TreatmentPlantService;
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
@RequestMapping("/api/v1/treatment-plants")
@Tag(name = "Treatment Plants", description = "Sewage Treatment Plant (STP) information — maintained by authorities")
// TreatmentPlantController: Manages Sewage Treatment Plant (STP) information — maintained by authorities
public class TreatmentPlantController {

    private final TreatmentPlantService treatmentPlantService;
    private final TreatmentPlantExternalService treatmentPlantExternalService;

    public TreatmentPlantController(TreatmentPlantService treatmentPlantService,
                                    TreatmentPlantExternalService treatmentPlantExternalService) {
        this.treatmentPlantService = treatmentPlantService;
        this.treatmentPlantExternalService = treatmentPlantExternalService;
    }

    @PostMapping
    @Operation(
            summary = "Create a treatment plant record",
            description = "Creates a Sewage Treatment Plant (STP) record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Treatment plant created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<TreatmentPlantResponse>> createPlant(@Valid @RequestBody TreatmentPlantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Treatment plant created successfully", treatmentPlantService.createTreatmentPlant(request)));
    }

    @GetMapping
    @Operation(
            summary = "List all treatment plants",
            description = "Returns all Sewage Treatment Plant (STP) records."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Treatment plants retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TreatmentPlantResponse>>> getAllPlants() {
        return ResponseEntity.ok(ApiResponse.success("Treatment plants retrieved successfully",
                treatmentPlantService.getAllTreatmentPlants()));
    }

    // getExternalPlants: Retrieves STP data from the Telangana government open data service
    @GetMapping("/external")
    @Operation(
            summary = "Fetch treatment plants from Telangana open data",
            description = "Returns STP location data retrieved from the Telangana government ArcGIS open data service."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Treatment plants retrieved from Telangana open data successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "External service error")
    })
    public ResponseEntity<ApiResponse<List<StpLocationData>>> getExternalPlants() {
        return ResponseEntity.ok(ApiResponse.success("Treatment plants retrieved from Telangana open data successfully",
                treatmentPlantExternalService.getStpLocations()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a treatment plant by id",
            description = "Returns a single treatment plant record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Treatment plant retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Treatment plant not found")
    })
    public ResponseEntity<ApiResponse<TreatmentPlantResponse>> getPlant(
            @Parameter(description = "Treatment plant id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Treatment plant retrieved successfully",
                treatmentPlantService.getTreatmentPlant(id)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a treatment plant record",
            description = "Updates an existing treatment plant record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Treatment plant updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Treatment plant not found")
    })
    public ResponseEntity<ApiResponse<TreatmentPlantResponse>> updatePlant(
            @Parameter(description = "Treatment plant id", example = "1") @PathVariable Long id, @Valid @RequestBody TreatmentPlantRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Treatment plant updated successfully",
                treatmentPlantService.updateTreatmentPlant(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a treatment plant record",
            description = "Deletes an existing treatment plant record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Treatment plant deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Treatment plant not found")
    })
    public ResponseEntity<ApiResponse<Void>> deletePlant(
            @Parameter(description = "Treatment plant id", example = "1") @PathVariable Long id) {
        treatmentPlantService.deleteTreatmentPlant(id);
        return ResponseEntity.ok(ApiResponse.success("Treatment plant deleted successfully", null));
    }
}
