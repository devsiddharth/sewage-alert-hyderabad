package com.sewagealert.community.service.impl;

import com.sewagealert.community.client.GooglePlacesClient;
import com.sewagealert.community.config.GooglePlacesProperties;
import com.sewagealert.community.dto.external.places.GooglePlacesDetails;
import com.sewagealert.community.dto.external.places.GooglePlacesDetailsResponse;
import com.sewagealert.community.dto.external.places.GooglePlacesResult;
import com.sewagealert.community.dto.external.places.GooglePlacesSearchResponse;
import com.sewagealert.community.dto.external.places.NgoSearchResult;
import com.sewagealert.community.exception.GooglePlacesException;
import com.sewagealert.community.service.NgoExternalService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * NgoExternalServiceImpl: Runs a Google Places Text Search for NGOs and enriches each result
 * with contact data (phone, website) via Place Details. Only sanitized results are returned.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NgoExternalServiceImpl implements NgoExternalService {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
    private static final String DETAILS_FIELDS =
            "name,formatted_address,formatted_phone_number,international_phone_number,website,rating";

    private final GooglePlacesClient googlePlacesClient;
    private final GooglePlacesProperties googlePlacesProperties;

    @Override
    public List<NgoSearchResult> searchNgos(String city, String keyword) {
        String apiKey = googlePlacesProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new GooglePlacesException("Google Places API key is not configured. Set community.external.google-places.api-key or the GOOGLE_PLACES_API_KEY environment variable.");
        }

        String query = buildQuery(city, keyword);
        log.info("Searching NGOs via Google Places Text Search — query='{}'", query);

        GooglePlacesSearchResponse searchResponse;
        try {
            // region=in biases results toward India, matching the Hyderabad use case
            searchResponse = googlePlacesClient.textSearch(query, apiKey, "in", "en");
        } catch (FeignException ex) {
            log.error("Google Places text search failed — status={}, error={}", ex.status(), ex.getMessage());
            throw new GooglePlacesException("Failed to query Google Places for NGO search", ex);
        }

        if (searchResponse == null) {
            throw new GooglePlacesException("Google Places returned an empty response");
        }
        if (!STATUS_OK.equals(searchResponse.getStatus())) {
            if (STATUS_ZERO_RESULTS.equals(searchResponse.getStatus())) {
                log.info("Google Places returned no NGO results for query '{}'", query);
                return List.of();
            }
            log.error("Google Places text search failed — status={}, error={}",
                    searchResponse.getStatus(), searchResponse.getErrorMessage());
            throw new GooglePlacesException("Google Places search failed with status: " + searchResponse.getStatus());
        }

        List<GooglePlacesResult> results = searchResponse.getResults() != null ? searchResponse.getResults() : List.of();
        int limit = Math.min(results.size(), googlePlacesProperties.getMaxResults());
        log.info("Google Places found {} results, enriching top {} with place details", results.size(), limit);

        List<NgoSearchResult> ngoResults = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            ngoResults.add(enrichWithDetails(results.get(i)));
        }
        return ngoResults;
    }

    private NgoSearchResult enrichWithDetails(GooglePlacesResult result) {
        NgoSearchResult ngo = new NgoSearchResult();
        ngo.setName(result.getName());
        ngo.setAddress(result.getFormattedAddress());
        ngo.setRating(result.getRating());
        if (result.getGeometry() != null && result.getGeometry().getLocation() != null) {
            ngo.setLatitude(result.getGeometry().getLocation().getLat());
            ngo.setLongitude(result.getGeometry().getLocation().getLng());
        }

        if (result.getPlaceId() != null) {
            GooglePlacesDetails details = fetchPlaceDetails(result.getPlaceId());
            if (details != null) {
                ngo.setPhone(firstNonBlank(details.getFormattedPhoneNumber(), details.getInternationalPhoneNumber()));
                ngo.setWebsite(details.getWebsite());
                if (ngo.getRating() == null) {
                    ngo.setRating(details.getRating());
                }
            }
        }
        return ngo;
    }

    private GooglePlacesDetails fetchPlaceDetails(String placeId) {
        try {
            GooglePlacesDetailsResponse response = googlePlacesClient.placeDetails(placeId, googlePlacesProperties.getApiKey(), DETAILS_FIELDS);
            if (response != null && STATUS_OK.equals(response.getStatus())) {
                return response.getResult();
            }
            log.warn("Google Places details returned status {} for place {}", response != null ? response.getStatus() : "null", placeId);
        } catch (FeignException ex) {
            log.warn("Google Places details failed for place {} — status={}", placeId, ex.status());
        }
        return null;
    }

    private String buildQuery(String city, String keyword) {
        String term = (keyword == null || keyword.isBlank()) ? "NGO" : keyword.trim();
        if (city != null && !city.isBlank()) {
            return term + " in " + city.trim();
        }
        return term;
    }

    private String firstNonBlank(String first, String second) {
        return (first != null && !first.isBlank()) ? first : second;
    }
}
