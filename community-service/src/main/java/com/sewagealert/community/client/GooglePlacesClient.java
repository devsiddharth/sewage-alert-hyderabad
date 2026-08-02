package com.sewagealert.community.client;

import com.sewagealert.community.dto.external.places.GooglePlacesDetailsResponse;
import com.sewagealert.community.dto.external.places.GooglePlacesSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * GooglePlacesClient: Declarative OpenFeign client for the Google Places API
 * (legacy Text Search + Place Details endpoints).
 * <p>
 * Google Places is an external public API — not registered in Eureka — so the base URL comes from
 * {@code community.external.google-places.base-url} configuration and load balancing is not applied.
 * Requires a Google Cloud API key with the Places API enabled (billing account linked).
 */
@FeignClient(name = "google-places", url = "${community.external.google-places.base-url}")
public interface GooglePlacesClient {

    /**
     * textSearch: Finds places matching a free-form query, e.g. "Water NGO in Hyderabad".
     * Does NOT return phone/website — those require a follow-up placeDetails call.
     */
    @GetMapping("/maps/api/place/textsearch/json")
    GooglePlacesSearchResponse textSearch(@RequestParam("query") String query,
                                          @RequestParam("key") String apiKey,
                                          @RequestParam(value = "region", required = false) String region,
                                          @RequestParam(value = "language", required = false) String language);

    /**
     * placeDetails: Enriches a place with contact fields (phone, website) and rating.
     * The fields param controls billing categories; only Basic/Contact fields are requested.
     */
    @GetMapping("/maps/api/place/details/json")
    GooglePlacesDetailsResponse placeDetails(@RequestParam("place_id") String placeId,
                                             @RequestParam("key") String apiKey,
                                             @RequestParam(value = "fields", required = false) String fields);
}
