package com.sewagealert.community.service;

import com.sewagealert.community.dto.external.places.NgoSearchResult;

import java.util.List;

/**
 * NgoExternalService: Discovers NGOs via the Google Places API.
 * Kept separate from the local NGO CRUD service.
 */
public interface NgoExternalService {

    /**
     * searchNgos: Searches for NGOs in a city matching a keyword.
     *
     * @param city    optional city filter, e.g. "Hyderabad"
     * @param keyword optional keyword, e.g. "Water NGO" (defaults to "NGO" when blank)
     */
    List<NgoSearchResult> searchNgos(String city, String keyword);
}
