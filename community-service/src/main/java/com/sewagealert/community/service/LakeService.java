package com.sewagealert.community.service;

import com.sewagealert.community.dto.LakeRequest;
import com.sewagealert.community.dto.LakeResponse;

import java.util.List;

// LakeService: Business logic for lake information and restoration status — maintained by authorities
public interface LakeService {

    // createLake: Creates a new lake record
    LakeResponse createLake(LakeRequest request);

    // getLake: Retrieves a single lake by its ID
    LakeResponse getLake(Long lakeId);

    // getAllLakes: Returns all lakes in the system
    List<LakeResponse> getAllLakes();

    // updateLake: Updates an existing lake's details
    LakeResponse updateLake(Long lakeId, LakeRequest request);

    // deleteLake: Removes a lake by its ID
    void deleteLake(Long lakeId);
}
