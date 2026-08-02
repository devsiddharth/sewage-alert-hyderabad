package com.sewagealert.community.dto.external.overpass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OverpassGeometryPoint: A single lat/lon vertex of a way/relation geometry.
 */
@Getter
@Setter
@NoArgsConstructor
public class OverpassGeometryPoint {

    private double lat;
    private double lon;
}
