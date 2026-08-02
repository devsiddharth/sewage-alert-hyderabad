package com.sewagealert.community.dto.external.overpass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OverpassBounds: Bounding box of an OSM element.
 */
@Getter
@Setter
@NoArgsConstructor
public class OverpassBounds {

    private double minlat;
    private double minlon;
    private double maxlat;
    private double maxlon;
}
