package com.sewagealert.community.dto.external.overpass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OverpassCenter: Computed center coordinate of a way or relation.
 */
@Getter
@Setter
@NoArgsConstructor
public class OverpassCenter {

    private double lat;
    private double lon;
}
