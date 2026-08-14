package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.PipelineRequest;
import com.sewagealert.community.dto.PipelineResponse;
import com.sewagealert.community.service.PipelineService;
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
@RequestMapping("/api/v1/pipelines")
@Tag(name = "Pipelines", description = "Sewage pipeline infrastructure information — maintained by authorities")
// PipelineController: Manages sewage pipeline infrastructure information — maintained by authorities
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping
    @Operation(
            summary = "Create a pipeline record",
            description = "Creates a sewage pipeline infrastructure record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pipeline created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<PipelineResponse>> createPipeline(@Valid @RequestBody PipelineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pipeline created successfully", pipelineService.createPipeline(request)));
    }

    @GetMapping
    @Operation(
            summary = "List pipelines",
            description = "Returns all pipeline records, optionally filtered by locality."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pipelines retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<PipelineResponse>>> getAllPipelines(
            @Parameter(description = "Optional locality filter", example = "Banjara Hills")
            @RequestParam(required = false) String locality) {
        if (locality != null) {
            return ResponseEntity.ok(ApiResponse.success("Pipelines retrieved successfully",
                    pipelineService.getPipelinesByLocality(locality)));
        }
        return ResponseEntity.ok(ApiResponse.success("Pipelines retrieved successfully", pipelineService.getAllPipelines()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a pipeline by id",
            description = "Returns a single pipeline record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pipeline retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pipeline not found")
    })
    public ResponseEntity<ApiResponse<PipelineResponse>> getPipeline(
            @Parameter(description = "Pipeline id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Pipeline retrieved successfully", pipelineService.getPipeline(id)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a pipeline record",
            description = "Updates an existing pipeline record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pipeline updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pipeline not found")
    })
    public ResponseEntity<ApiResponse<PipelineResponse>> updatePipeline(
            @Parameter(description = "Pipeline id", example = "1") @PathVariable Long id, @Valid @RequestBody PipelineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pipeline updated successfully",
                pipelineService.updatePipeline(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a pipeline record",
            description = "Deletes an existing pipeline record."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pipeline deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pipeline not found")
    })
    public ResponseEntity<ApiResponse<Void>> deletePipeline(
            @Parameter(description = "Pipeline id", example = "1") @PathVariable Long id) {
        pipelineService.deletePipeline(id);
        return ResponseEntity.ok(ApiResponse.success("Pipeline deleted successfully", null));
    }
}
