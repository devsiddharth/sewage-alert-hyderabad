package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.PipelineRequest;
import com.sewagealert.community.dto.PipelineResponse;
import com.sewagealert.community.service.PipelineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pipelines")
// PipelineController: Manages sewage pipeline infrastructure information — maintained by authorities
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PipelineResponse>> createPipeline(@Valid @RequestBody PipelineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pipeline created successfully", pipelineService.createPipeline(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PipelineResponse>>> getAllPipelines(
            @RequestParam(required = false) String locality) {
        if (locality != null) {
            return ResponseEntity.ok(ApiResponse.success("Pipelines retrieved successfully",
                    pipelineService.getPipelinesByLocality(locality)));
        }
        return ResponseEntity.ok(ApiResponse.success("Pipelines retrieved successfully", pipelineService.getAllPipelines()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PipelineResponse>> getPipeline(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Pipeline retrieved successfully", pipelineService.getPipeline(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PipelineResponse>> updatePipeline(
            @PathVariable Long id, @Valid @RequestBody PipelineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pipeline updated successfully",
                pipelineService.updatePipeline(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePipeline(@PathVariable Long id) {
        pipelineService.deletePipeline(id);
        return ResponseEntity.ok(ApiResponse.success("Pipeline deleted successfully", null));
    }
}
