package com.sewagealert.community.dto.external.overpass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * LakeAddress: Address information assembled from OSM addr:* tags, when available.
 */
@Getter
@Setter
@NoArgsConstructor
public class LakeAddress {

    private String street;
    private String city;
    private String postcode;
    private String state;
    private String formatted;
}
