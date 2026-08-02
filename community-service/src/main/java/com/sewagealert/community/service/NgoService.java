package com.sewagealert.community.service;

import com.sewagealert.community.dto.NgoRequest;
import com.sewagealert.community.dto.NgoResponse;

import java.util.List;

// NgoService: Business logic for NGO information — managed by administrators
public interface NgoService {

    // createNgo: Creates a new NGO record
    NgoResponse createNgo(NgoRequest request);

    // getNgo: Retrieves a single NGO by its ID
    NgoResponse getNgo(Long ngoId);

    // getAllNgos: Returns all NGOs in the system
    List<NgoResponse> getAllNgos();

    // updateNgo: Updates an existing NGO's details
    NgoResponse updateNgo(Long ngoId, NgoRequest request);

    // deleteNgo: Removes an NGO by its ID
    void deleteNgo(Long ngoId);
}
