package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.PipelineRequest;
import com.sewagealert.community.dto.PipelineResponse;
import com.sewagealert.community.model.Pipeline;
import com.sewagealert.community.repository.PipelineRepository;
import com.sewagealert.community.service.PipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// PipelineServiceImpl: Core business logic for sewage pipeline infrastructure — maintained by authorities
public class PipelineServiceImpl implements PipelineService {

    private final PipelineRepository pipelineRepository;

    @Override
    // createPipeline: Creates a new pipeline record
    public PipelineResponse createPipeline(PipelineRequest request) {
        Pipeline pipeline = new Pipeline();
        pipeline.setLocality(request.getLocality());
        pipeline.setInstallationYear(request.getInstallationYear());
        pipeline.setDesignedCapacity(request.getDesignedCapacity());
        pipeline.setMaintenanceDate(request.getMaintenanceDate());
        pipeline.setOperationalStatus(request.getOperationalStatus());
        pipeline.setNotes(request.getNotes());

        pipeline = pipelineRepository.save(pipeline);
        log.info("Pipeline created for locality: {}", pipeline.getLocality());

        return PipelineResponse.fromEntity(pipeline);
    }

    @Override
    // getPipeline: Retrieves a single pipeline by its ID
    public PipelineResponse getPipeline(Long pipelineId) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new RuntimeException("Pipeline not found with id: " + pipelineId));
        return PipelineResponse.fromEntity(pipeline);
    }

    @Override
    // getAllPipelines: Returns all pipelines in the system
    public List<PipelineResponse> getAllPipelines() {
        return pipelineRepository.findAll().stream()
                .map(PipelineResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // getPipelinesByLocality: Returns all pipelines matching a given locality (case-insensitive)
    public List<PipelineResponse> getPipelinesByLocality(String locality) {
        return pipelineRepository.findByLocalityContainingIgnoreCase(locality).stream()
                .map(PipelineResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // updatePipeline: Updates an existing pipeline's details
    public PipelineResponse updatePipeline(Long pipelineId, PipelineRequest request) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new RuntimeException("Pipeline not found with id: " + pipelineId));

        pipeline.setLocality(request.getLocality());
        pipeline.setInstallationYear(request.getInstallationYear());
        pipeline.setDesignedCapacity(request.getDesignedCapacity());
        pipeline.setMaintenanceDate(request.getMaintenanceDate());
        pipeline.setOperationalStatus(request.getOperationalStatus());
        pipeline.setNotes(request.getNotes());

        pipeline = pipelineRepository.save(pipeline);
        log.info("Pipeline updated: {}", pipelineId);

        return PipelineResponse.fromEntity(pipeline);
    }

    @Override
    // deletePipeline: Removes a pipeline by its ID
    public void deletePipeline(Long pipelineId) {
        pipelineRepository.deleteById(pipelineId);
        log.info("Pipeline deleted: {}", pipelineId);
    }
}
