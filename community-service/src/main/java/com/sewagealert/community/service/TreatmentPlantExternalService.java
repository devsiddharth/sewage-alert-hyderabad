package com.sewagealert.community.service;

import com.sewagealert.community.dto.external.telangana.StpLocationData;

import java.util.List;

/**
 * TreatmentPlantExternalService: Retrieves sewage treatment plant (STP) locations from the
 * Telangana Government open data service (ArcGIS REST). Kept separate from the local CRUD service.
 */
public interface TreatmentPlantExternalService {

    /**
     * getStpLocations: Fetches all STP records for the Telangana core urban region
     * (name, coordinates, capacity, technology, operational status, etc.).
     */
    List<StpLocationData> getStpLocations();
}
