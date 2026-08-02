package com.sewagealert.community.dto.external.places;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GooglePlacesGeometry: Geometry block of a Google Places result.
 */
@Getter
@Setter
@NoArgsConstructor
public class GooglePlacesGeometry {

    private GooglePlacesLocation location;
}
