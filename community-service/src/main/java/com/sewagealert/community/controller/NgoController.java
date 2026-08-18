package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.NgoRequest;
import com.sewagealert.community.dto.NgoResponse;
import com.sewagealert.community.dto.external.places.NgoSearchResult;
import com.sewagealert.community.service.NgoExternalService;
import com.sewagealert.community.service.NgoService;
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
@RequestMapping("/api/v1/ngos")
@Tag(name = "NGOs", description = "NGO information CRUD — managed by administrators")
// NgoController: CRUD operations for NGO information — managed by administrators
public class NgoController {

    private final NgoService ngoService;
    private final NgoExternalService ngoExternalService;

    public NgoController(NgoService ngoService, NgoExternalService ngoExternalService) {
        this.ngoService = ngoService;
        this.ngoExternalService = ngoExternalService;
    }

    @PostMapping
    @Operation(
            summary = "Create an NGO record",
            description = "Creates an NGO information record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "NGO created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<NgoResponse>> createNgo(@Valid @RequestBody NgoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("NGO created successfully", ngoService.createNgo(request)));
    }

    @GetMapping
    @Operation(
            summary = "List all NGOs",
            description = "Returns all NGO records."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "NGOs retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<NgoResponse>>> getAllNgos() {
        return ResponseEntity.ok(ApiResponse.success("NGOs retrieved successfully", ngoService.getAllNgos()));
    }

    // searchNgos: Discovers NGOs via the external Google Places API
    @GetMapping("/search")
    @Operation(
            summary = "Search NGOs (external Google Places)",
            description = "Discovers NGOs via the external Google Places API, optionally filtered by city/keyword. "
                    + "Falls back to the free Overpass API when no Google Places key is configured."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "NGO search completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "External service error")
    })
    public ResponseEntity<ApiResponse<List<NgoSearchResult>>> searchNgos(
            @Parameter(description = "Optional city filter", example = "Hyderabad") @RequestParam(required = false) String city,
            @Parameter(description = "Optional keyword filter", example = "water") @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("NGO search completed successfully",
                ngoExternalService.searchNgos(city, keyword)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get an NGO by id",
            description = "Returns a single NGO record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "NGO retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NGO not found")
    })
    public ResponseEntity<ApiResponse<NgoResponse>> getNgo(
            @Parameter(description = "NGO id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("NGO retrieved successfully", ngoService.getNgo(id)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an NGO record",
            description = "Updates an existing NGO record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "NGO updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NGO not found")
    })
    public ResponseEntity<ApiResponse<NgoResponse>> updateNgo(
            @Parameter(description = "NGO id", example = "1") @PathVariable Long id, @Valid @RequestBody NgoRequest request) {
        return ResponseEntity.ok(ApiResponse.success("NGO updated successfully", ngoService.updateNgo(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an NGO record",
            description = "Deletes an existing NGO record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "NGO deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "NGO not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteNgo(
            @Parameter(description = "NGO id", example = "1") @PathVariable Long id) {
        ngoService.deleteNgo(id);
        return ResponseEntity.ok(ApiResponse.success("NGO deleted successfully", null));
    }
}
