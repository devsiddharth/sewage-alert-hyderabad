package com.sewagealert.community.dto.external.places;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GooglePlacesLocation: Lat/lng coordinates of a Google Places result.
 */
@Getter
@Setter
@NoArgsConstructor
public class GooglePlacesLocation {

    private double lat;
    private double lng;
}
