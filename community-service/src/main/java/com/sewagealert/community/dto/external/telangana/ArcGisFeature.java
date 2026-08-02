package com.sewagealert.community.dto.external.telangana;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ArcGisFeature: A single feature (STP record) returned by the ArcGIS REST service.
 * Coordinates are available in both {@code attributes} (WGS84 lat/lon) and
 * {@code geometry} (projected UTM x/y) — attributes are preferred.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArcGisFeature {

    private ArcGisAttributes attributes;
    private ArcGisGeometry geometry;
}
