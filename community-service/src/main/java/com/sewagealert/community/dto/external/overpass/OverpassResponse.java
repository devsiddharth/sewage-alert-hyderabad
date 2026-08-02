package com.sewagealert.community.dto.external.overpass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OverpassResponse: Raw response envelope from the OpenStreetMap Overpass API.
 * Used only for deserialization — never exposed to API callers.
 */
@Getter
@Setter
@NoArgsConstructor
public class OverpassResponse {

    private String version;
    private String generator;
    private List<OverpassElement> elements;
}
