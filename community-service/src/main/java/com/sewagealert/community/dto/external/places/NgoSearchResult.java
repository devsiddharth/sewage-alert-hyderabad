package com.sewagealert.community.dto.external.places;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NgoSearchResult: The sanitized NGO representation returned by the /ngos/search endpoint.
 * Combines Text Search results enriched with contact data from Place Details.
 */
@Getter
@Setter
@NoArgsConstructor
public class NgoSearchResult {

    private String name;
    private String address;
    private String phone;
    private Double rating;
    private String website;
    private Double latitude;
    private Double longitude;
}
