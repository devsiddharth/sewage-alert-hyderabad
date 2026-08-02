package com.sewagealert.community.service;

import com.sewagealert.community.dto.external.overpass.LakeGeoData;

import java.util.List;

/**
 * LakeExternalService: Retrieves lake location and geometry data from the OpenStreetMap
 * Overpass API. Kept separate from the local Lake CRUD service.
 */
public interface LakeExternalService {

    /**
     * getLakes: Fetches all named lakes within the configured bounding box.
     * Returns only elements that have a name tag.
     */
    List<LakeGeoData> getLakes();
}
