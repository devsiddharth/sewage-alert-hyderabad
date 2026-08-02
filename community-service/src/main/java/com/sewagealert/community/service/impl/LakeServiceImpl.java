package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.LakeRequest;
import com.sewagealert.community.dto.LakeResponse;
import com.sewagealert.community.model.Lake;
import com.sewagealert.community.repository.LakeRepository;
import com.sewagealert.community.service.LakeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// LakeServiceImpl: Core business logic for lake information and restoration status — maintained by authorities
public class LakeServiceImpl implements LakeService {

    private final LakeRepository lakeRepository;

    @Override
    // createLake: Creates a new lake record
    public LakeResponse createLake(LakeRequest request) {
        Lake lake = new Lake();
        lake.setName(request.getName());
        lake.setLocation(request.getLocation());
        lake.setRestorationStatus(request.getRestorationStatus());
        lake.setWaterSource(request.getWaterSource());
        lake.setConnectedStpId(request.getConnectedStpId());
        lake.setEnvironmentalUpdates(request.getEnvironmentalUpdates());
        lake.setDescription(request.getDescription());

        lake = lakeRepository.save(lake);
        log.info("Lake created: {}", lake.getName());

        return LakeResponse.fromEntity(lake);
    }

    @Override
    // getLake: Retrieves a single lake by its ID
    public LakeResponse getLake(Long lakeId) {
        Lake lake = lakeRepository.findById(lakeId)
                .orElseThrow(() -> new RuntimeException("Lake not found with id: " + lakeId));
        return LakeResponse.fromEntity(lake);
    }

    @Override
    // getAllLakes: Returns all lakes in the system
    public List<LakeResponse> getAllLakes() {
        return lakeRepository.findAll().stream()
                .map(LakeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // updateLake: Updates an existing lake's details
    public LakeResponse updateLake(Long lakeId, LakeRequest request) {
        Lake lake = lakeRepository.findById(lakeId)
                .orElseThrow(() -> new RuntimeException("Lake not found with id: " + lakeId));

        lake.setName(request.getName());
        lake.setLocation(request.getLocation());
        lake.setRestorationStatus(request.getRestorationStatus());
        lake.setWaterSource(request.getWaterSource());
        lake.setConnectedStpId(request.getConnectedStpId());
        lake.setEnvironmentalUpdates(request.getEnvironmentalUpdates());
        lake.setDescription(request.getDescription());

        lake = lakeRepository.save(lake);
        log.info("Lake updated: {}", lakeId);

        return LakeResponse.fromEntity(lake);
    }

    @Override
    // deleteLake: Removes a lake by its ID
    public void deleteLake(Long lakeId) {
        lakeRepository.deleteById(lakeId);
        log.info("Lake deleted: {}", lakeId);
    }
}
