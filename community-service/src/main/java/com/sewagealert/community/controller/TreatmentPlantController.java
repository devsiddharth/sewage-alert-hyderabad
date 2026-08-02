package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.TreatmentPlantRequest;
import com.sewagealert.community.dto.TreatmentPlantResponse;
import com.sewagealert.community.dto.external.telangana.StpLocationData;
import com.sewagealert.community.service.TreatmentPlantExternalService;
import com.sewagealert.community.service.TreatmentPlantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/treatment-plants")
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
    public ResponseEntity<ApiResponse<TreatmentPlantResponse>> createPlant(@Valid @RequestBody TreatmentPlantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Treatment plant created successfully", treatmentPlantService.createTreatmentPlant(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TreatmentPlantResponse>>> getAllPlants() {
        return ResponseEntity.ok(ApiResponse.success("Treatment plants retrieved successfully",
                treatmentPlantService.getAllTreatmentPlants()));
    }

    // getExternalPlants: Retrieves STP data from the Telangana government open data service
    @GetMapping("/external")
    public ResponseEntity<ApiResponse<List<StpLocationData>>> getExternalPlants() {
        return ResponseEntity.ok(ApiResponse.success("Treatment plants retrieved from Telangana open data successfully",
                treatmentPlantExternalService.getStpLocations()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TreatmentPlantResponse>> getPlant(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Treatment plant retrieved successfully",
                treatmentPlantService.getTreatmentPlant(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TreatmentPlantResponse>> updatePlant(
            @PathVariable Long id, @Valid @RequestBody TreatmentPlantRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Treatment plant updated successfully",
                treatmentPlantService.updateTreatmentPlant(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlant(@PathVariable Long id) {
        treatmentPlantService.deleteTreatmentPlant(id);
        return ResponseEntity.ok(ApiResponse.success("Treatment plant deleted successfully", null));
    }
}
