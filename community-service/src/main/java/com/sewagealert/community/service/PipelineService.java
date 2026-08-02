package com.sewagealert.community.service;

import com.sewagealert.community.dto.PipelineRequest;
import com.sewagealert.community.dto.PipelineResponse;

import java.util.List;

// PipelineService: Business logic for sewage pipeline infrastructure information — maintained by authorities
public interface PipelineService {

    // createPipeline: Creates a new pipeline record
    PipelineResponse createPipeline(PipelineRequest request);

    // getPipeline: Retrieves a single pipeline by its ID
    PipelineResponse getPipeline(Long pipelineId);

    // getAllPipelines: Returns all pipelines in the system
    List<PipelineResponse> getAllPipelines();

    // getPipelinesByLocality: Returns all pipelines matching a given locality (case-insensitive)
    List<PipelineResponse> getPipelinesByLocality(String locality);

    // updatePipeline: Updates an existing pipeline's details
    PipelineResponse updatePipeline(Long pipelineId, PipelineRequest request);

    // deletePipeline: Removes a pipeline by its ID
    void deletePipeline(Long pipelineId);
}
