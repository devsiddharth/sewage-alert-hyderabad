package com.sewagealert.community.dto.external.telangana;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * ArcGisQueryResponse: Raw response from the Telangana ArcGIS REST query endpoint.
 * On failure the service returns HTTP 200 with an {@code error} block instead of {@code features}.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArcGisQueryResponse {

    private ArcGisError error;
    private List<ArcGisFeature> features;
}
