package com.sewagealert.community.dto.external.places;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GooglePlacesResult: A single place returned by Google Places Text Search.
 * Contact fields (phone, website) are NOT present here — they require a Place Details call.
 */
@Getter
@Setter
@NoArgsConstructor
public class GooglePlacesResult {

    private String name;
    private String formattedAddress;
    private String placeId;
    private Double rating;
    private GooglePlacesGeometry geometry;
}
