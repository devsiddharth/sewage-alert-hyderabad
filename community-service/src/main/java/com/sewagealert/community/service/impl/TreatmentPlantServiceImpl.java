package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.TreatmentPlantRequest;
import com.sewagealert.community.dto.TreatmentPlantResponse;
import com.sewagealert.community.model.TreatmentPlant;
import com.sewagealert.community.repository.TreatmentPlantRepository;
import com.sewagealert.community.service.TreatmentPlantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// TreatmentPlantServiceImpl: Core business logic for Sewage Treatment Plant (STP) information — maintained by authorities
public class TreatmentPlantServiceImpl implements TreatmentPlantService {

    private final TreatmentPlantRepository treatmentPlantRepository;

    @Override
    // createTreatmentPlant: Creates a new treatment plant record
    public TreatmentPlantResponse createTreatmentPlant(TreatmentPlantRequest request) {
        TreatmentPlant plant = new TreatmentPlant();
        plant.setName(request.getName());
        plant.setCapacityMld(request.getCapacityMld());
        plant.setLocation(request.getLocation());
        plant.setTreatmentMethod(request.getTreatmentMethod());
        plant.setWaterReuseInfo(request.getWaterReuseInfo());
        plant.setDescription(request.getDescription());

        plant = treatmentPlantRepository.save(plant);
        log.info("Treatment plant created: {}", plant.getName());

        return TreatmentPlantResponse.fromEntity(plant);
    }

    @Override
    // getTreatmentPlant: Retrieves a single treatment plant by its ID
    public TreatmentPlantResponse getTreatmentPlant(Long plantId) {
        TreatmentPlant plant = treatmentPlantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Treatment plant not found with id: " + plantId));
        return TreatmentPlantResponse.fromEntity(plant);
    }

    @Override
    // getAllTreatmentPlants: Returns all treatment plants in the system
    public List<TreatmentPlantResponse> getAllTreatmentPlants() {
        return treatmentPlantRepository.findAll().stream()
                .map(TreatmentPlantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // updateTreatmentPlant: Updates an existing treatment plant's details
    public TreatmentPlantResponse updateTreatmentPlant(Long plantId, TreatmentPlantRequest request) {
        TreatmentPlant plant = treatmentPlantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Treatment plant not found with id: " + plantId));

        plant.setName(request.getName());
        plant.setCapacityMld(request.getCapacityMld());
        plant.setLocation(request.getLocation());
        plant.setTreatmentMethod(request.getTreatmentMethod());
        plant.setWaterReuseInfo(request.getWaterReuseInfo());
        plant.setDescription(request.getDescription());

        plant = treatmentPlantRepository.save(plant);
        log.info("Treatment plant updated: {}", plantId);

        return TreatmentPlantResponse.fromEntity(plant);
    }

    @Override
    // deleteTreatmentPlant: Removes a treatment plant by its ID
    public void deleteTreatmentPlant(Long plantId) {
        treatmentPlantRepository.deleteById(plantId);
        log.info("Treatment plant deleted: {}", plantId);
    }
}
