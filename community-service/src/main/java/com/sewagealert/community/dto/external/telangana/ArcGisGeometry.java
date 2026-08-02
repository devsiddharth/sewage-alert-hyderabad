package com.sewagealert.community.dto.external.telangana;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ArcGisGeometry: Projected geometry of a feature (UTM Zone 44N x/y).
 * The WGS84 latitude/longitude attributes are preferred over these projected coordinates.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArcGisGeometry {

    private double x;
    private double y;
}
