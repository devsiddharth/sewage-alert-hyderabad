package com.sewagealert.community.dto.external.overpass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * LakeGeoData: The sanitized lake representation returned by the /lakes/external endpoint.
 * Includes the lake name, representative coordinate, full geometry, and address (if tagged).
 */
@Getter
@Setter
@NoArgsConstructor
public class LakeGeoData {

    private String name;
    private Double latitude;
    private Double longitude;
    private List<OverpassGeometryPoint> geometry;
    private LakeAddress address;
}
