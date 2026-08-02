package com.sewagealert.community.dto.external.places;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GooglePlacesDetails: Contact/atmosphere fields returned only by the Place Details endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
public class GooglePlacesDetails {

    private String name;
    private String formattedAddress;
    private String formattedPhoneNumber;
    private String internationalPhoneNumber;
    private String website;
    private Double rating;
}
