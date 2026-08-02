package com.sewagealert.community.dto.external.places;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * GooglePlacesSearchResponse: Raw response from the Google Places Text Search endpoint.
 * Only the status and results are of interest — the raw payload is never exposed to callers.
 */
@Getter
@Setter
@NoArgsConstructor
public class GooglePlacesSearchResponse {

    private String status;
    private String errorMessage;
    private List<GooglePlacesResult> results;
}
