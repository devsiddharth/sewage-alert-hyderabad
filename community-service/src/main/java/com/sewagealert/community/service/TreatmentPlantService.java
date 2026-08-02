package com.sewagealert.community.service;

import com.sewagealert.community.dto.TreatmentPlantRequest;
import com.sewagealert.community.dto.TreatmentPlantResponse;

import java.util.List;

// TreatmentPlantService: Business logic for Sewage Treatment Plant (STP) information — maintained by authorities
public interface TreatmentPlantService {

    // createTreatmentPlant: Creates a new treatment plant record
    TreatmentPlantResponse createTreatmentPlant(TreatmentPlantRequest request);

    // getTreatmentPlant: Retrieves a single treatment plant by its ID
    TreatmentPlantResponse getTreatmentPlant(Long plantId);

    // getAllTreatmentPlants: Returns all treatment plants in the system
    List<TreatmentPlantResponse> getAllTreatmentPlants();

    // updateTreatmentPlant: Updates an existing treatment plant's details
    TreatmentPlantResponse updateTreatmentPlant(Long plantId, TreatmentPlantRequest request);

    // deleteTreatmentPlant: Removes a treatment plant by its ID
    void deleteTreatmentPlant(Long plantId);
}
