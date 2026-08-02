package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.LakeRequest;
import com.sewagealert.community.dto.LakeResponse;
import com.sewagealert.community.dto.external.overpass.LakeGeoData;
import com.sewagealert.community.service.LakeExternalService;
import com.sewagealert.community.service.LakeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lakes")
// LakeController: Manages lake information and restoration status — maintained by authorities
public class LakeController {

    private final LakeService lakeService;
    private final LakeExternalService lakeExternalService;

    public LakeController(LakeService lakeService, LakeExternalService lakeExternalService) {
        this.lakeService = lakeService;
        this.lakeExternalService = lakeExternalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LakeResponse>> createLake(@Valid @RequestBody LakeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lake created successfully", lakeService.createLake(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LakeResponse>>> getAllLakes() {
        return ResponseEntity.ok(ApiResponse.success("Lakes retrieved successfully", lakeService.getAllLakes()));
    }

    // getExternalLakes: Retrieves lake data from the external OpenStreetMap Overpass API
    @GetMapping("/external")
    public ResponseEntity<ApiResponse<List<LakeGeoData>>> getExternalLakes() {
        return ResponseEntity.ok(ApiResponse.success("Lakes retrieved from OpenStreetMap successfully",
                lakeExternalService.getLakes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LakeResponse>> getLake(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lake retrieved successfully", lakeService.getLake(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LakeResponse>> updateLake(
            @PathVariable Long id, @Valid @RequestBody LakeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Lake updated successfully", lakeService.updateLake(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLake(@PathVariable Long id) {
        lakeService.deleteLake(id);
        return ResponseEntity.ok(ApiResponse.success("Lake deleted successfully", null));
    }
}
