package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.NgoRequest;
import com.sewagealert.community.dto.NgoResponse;
import com.sewagealert.community.dto.external.places.NgoSearchResult;
import com.sewagealert.community.service.NgoExternalService;
import com.sewagealert.community.service.NgoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ngos")
// NgoController: CRUD operations for NGO information — managed by administrators
public class NgoController {

    private final NgoService ngoService;
    private final NgoExternalService ngoExternalService;

    public NgoController(NgoService ngoService, NgoExternalService ngoExternalService) {
        this.ngoService = ngoService;
        this.ngoExternalService = ngoExternalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NgoResponse>> createNgo(@Valid @RequestBody NgoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("NGO created successfully", ngoService.createNgo(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NgoResponse>>> getAllNgos() {
        return ResponseEntity.ok(ApiResponse.success("NGOs retrieved successfully", ngoService.getAllNgos()));
    }

    // searchNgos: Discovers NGOs via the external Google Places API
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<NgoSearchResult>>> searchNgos(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success("NGO search completed successfully",
                ngoExternalService.searchNgos(city, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NgoResponse>> getNgo(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("NGO retrieved successfully", ngoService.getNgo(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NgoResponse>> updateNgo(@PathVariable Long id, @Valid @RequestBody NgoRequest request) {
        return ResponseEntity.ok(ApiResponse.success("NGO updated successfully", ngoService.updateNgo(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNgo(@PathVariable Long id) {
        ngoService.deleteNgo(id);
        return ResponseEntity.ok(ApiResponse.success("NGO deleted successfully", null));
    }
}
