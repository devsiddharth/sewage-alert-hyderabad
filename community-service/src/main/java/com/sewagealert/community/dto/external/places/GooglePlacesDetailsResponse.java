package com.sewagealert.community.dto.external.places;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GooglePlacesDetailsResponse: Raw response from the Google Places Place Details endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
public class GooglePlacesDetailsResponse {

    private String status;
    private String errorMessage;
    private GooglePlacesDetails result;
}
