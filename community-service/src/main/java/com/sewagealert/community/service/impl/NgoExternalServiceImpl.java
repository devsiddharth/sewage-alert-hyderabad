package com.sewagealert.community.service.impl;

import com.sewagealert.community.client.GooglePlacesClient;
import com.sewagealert.community.client.OverpassClient;
import com.sewagealert.community.config.GooglePlacesProperties;
import com.sewagealert.community.config.OverpassProperties;
import com.sewagealert.community.dto.external.overpass.OverpassElement;
import com.sewagealert.community.dto.external.overpass.OverpassResponse;
import com.sewagealert.community.dto.external.places.GooglePlacesDetails;
import com.sewagealert.community.dto.external.places.GooglePlacesDetailsResponse;
import com.sewagealert.community.dto.external.places.GooglePlacesResult;
import com.sewagealert.community.dto.external.places.GooglePlacesSearchResponse;
import com.sewagealert.community.dto.external.places.NgoSearchResult;
import com.sewagealert.community.exception.GooglePlacesException;
import com.sewagealert.community.exception.LocationServiceException;
import com.sewagealert.community.service.NgoExternalService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NgoExternalServiceImpl: Discovers NGOs via the Google Places API and enriches each result
 * with contact data (phone, website) via Place Details. Only sanitized results are returned.
 * <p>
 * When the Google Places API key is not configured (it requires paid billing), the service
 * transparently falls back to the free OpenStreetMap Overpass API and searches for NGO-type
 * offices inside the configured Hyderabad bounding box — so the public NGOs page still shows
 * live data without a paid key.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NgoExternalServiceImpl implements NgoExternalService {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
    private static final String DETAILS_FIELDS =
            "name,formatted_address,formatted_phone_number,international_phone_number,website,rating";

    // Overpass query: any node/way/relation tagged as an NGO, charity, non-profit or social
    // facility inside the bounding box. `out body center` gives node coords and way/relation centers.
    private static final String OVERPASS_QUERY_TEMPLATE =
            "[out:json][timeout:%d];" +
            "(" +
            "nwr[\"office\"=\"ngo\"](%s);" +
            "nwr[\"office\"=\"charity\"](%s);" +
            "nwr[\"office\"=\"non_profit\"](%s);" +
            "nwr[\"amenity\"=\"social_facility\"](%s);" +
            ");" +
            "out body center tags;";

    private final GooglePlacesClient googlePlacesClient;
    private final GooglePlacesProperties googlePlacesProperties;
    private final OverpassClient overpassClient;
    private final OverpassProperties overpassProperties;

    @Override
    public List<NgoSearchResult> searchNgos(String city, String keyword) {
        String apiKey = googlePlacesProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            // Free fallback — no Google billing required.
            return searchNgosViaOverpass();
        }
        return searchNgosViaGooglePlaces(city, keyword, apiKey);
    }

    // ------------------------------------------------------------------
    // Primary path: Google Places (requires a billed API key)
    // ------------------------------------------------------------------

    private List<NgoSearchResult> searchNgosViaGooglePlaces(String city, String keyword, String apiKey) {
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
            ngoResults.add(enrichWithDetails(results.get(i), apiKey));
        }
        return ngoResults;
    }

    private NgoSearchResult enrichWithDetails(GooglePlacesResult result, String apiKey) {
        NgoSearchResult ngo = new NgoSearchResult();
        ngo.setName(result.getName());
        ngo.setAddress(result.getFormattedAddress());
        ngo.setRating(result.getRating());
        if (result.getGeometry() != null && result.getGeometry().getLocation() != null) {
            ngo.setLatitude(result.getGeometry().getLocation().getLat());
            ngo.setLongitude(result.getGeometry().getLocation().getLng());
        }

        if (result.getPlaceId() != null) {
            GooglePlacesDetails details = fetchPlaceDetails(result.getPlaceId(), apiKey);
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

    private GooglePlacesDetails fetchPlaceDetails(String placeId, String apiKey) {
        try {
            GooglePlacesDetailsResponse response = googlePlacesClient.placeDetails(placeId, apiKey, DETAILS_FIELDS);
            if (response != null && STATUS_OK.equals(response.getStatus())) {
                return response.getResult();
            }
            log.warn("Google Places details returned status {} for place {}",
                    response != null ? response.getStatus() : "null", placeId);
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

    // ------------------------------------------------------------------
    // Free fallback path: OpenStreetMap Overpass (no API key required)
    // ------------------------------------------------------------------

    private List<NgoSearchResult> searchNgosViaOverpass() {
        OverpassProperties.Bounds bbox = overpassProperties.getBbox();
        String bboxString = String.format("%s,%s,%s,%s", bbox.getSouth(), bbox.getWest(), bbox.getNorth(), bbox.getEast());
        String query = String.format(OVERPASS_QUERY_TEMPLATE,
                overpassProperties.getQueryTimeoutSeconds(), bboxString, bboxString, bboxString, bboxString);
        String encodedBody = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        log.info("Google Places key not configured — querying Overpass for NGO offices in bbox ({})", bboxString);

        OverpassResponse response = queryOverpassWithRetry(encodedBody);

        if (response == null || response.getElements() == null) {
            log.warn("Overpass API returned no elements");
            return List.of();
        }

        List<NgoSearchResult> results = response.getElements().stream()
                .map(this::toNgoSearchResult)
                .filter(Objects::nonNull)
                .limit(overpassProperties.getMaxElements())
                .collect(Collectors.toList());
        log.info("Overpass returned {} elements; mapped {} named NGO offices", response.getElements().size(), results.size());
        return results;
    }

    /**
     * queryOverpassWithRetry: The public Overpass API queues queries under load and can answer
     * with 429/504 transient errors. Retry a couple of times with a short pause before giving up.
     */
    private OverpassResponse queryOverpassWithRetry(String encodedBody) {
        int attempts = 0;
        int maxAttempts = 3;
        while (true) {
            attempts++;
            try {
                return overpassClient.query(encodedBody);
            } catch (FeignException ex) {
                boolean retryable = ex.status() == 429 || ex.status() == 503 || ex.status() == 504 || ex.status() >= 500;
                if (!retryable || attempts >= maxAttempts) {
                    log.error("Overpass API request failed — status={}, error={}", ex.status(), ex.getMessage());
                    throw new LocationServiceException("Failed to fetch NGO data from the OpenStreetMap Overpass API", ex);
                }
                log.warn("Overpass API transient failure (status={}), retrying {}/{}", ex.status(), attempts, maxAttempts);
                try {
                    Thread.sleep(2000L * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LocationServiceException("Interrupted while retrying Overpass API request", ie);
                }
            }
        }
    }

    private NgoSearchResult toNgoSearchResult(OverpassElement element) {
        Map<String, String> tags = element.getTags();
        if (tags == null || isBlank(tags.get("name"))) {
            return null; // only named organisations are useful for the UI
        }

        NgoSearchResult ngo = new NgoSearchResult();
        ngo.setName(tags.get("name"));
        ngo.setAddress(buildAddress(tags));
        ngo.setPhone(firstNonBlank(tags.get("phone"), tags.get("contact:phone")));
        ngo.setWebsite(firstNonBlank(tags.get("website"), tags.get("contact:website")));

        if (element.getLat() != null && element.getLon() != null) {
            ngo.setLatitude(element.getLat());
            ngo.setLongitude(element.getLon());
        } else if (element.getCenter() != null) {
            ngo.setLatitude(element.getCenter().getLat());
            ngo.setLongitude(element.getCenter().getLon());
        }
        return ngo;
    }

    private String buildAddress(Map<String, String> tags) {
        String formatted = Stream.of(
                        tags.get("addr:housenumber"), tags.get("addr:street"),
                        tags.get("addr:city"), tags.get("addr:postcode"))
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
        return formatted.isEmpty() ? null : formatted;
    }

    private String firstNonBlank(String first, String second) {
        return (first != null && !first.isBlank()) ? first : second;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
