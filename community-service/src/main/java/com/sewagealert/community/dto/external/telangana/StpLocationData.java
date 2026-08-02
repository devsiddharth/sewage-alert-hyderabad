package com.sewagealert.community.dto.external.telangana;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * StpLocationData: The sanitized STP representation returned by the /treatment-plants/external
 * endpoint, mapped from the Telangana government open data service.
 */
@Getter
@Setter
@NoArgsConstructor
public class StpLocationData {

    private String name;
    private Double latitude;
    private Double longitude;
    private Double yearOfCommissioning;
    private String operationalStatus;
    private Double installedCapacityMld;
    private Double utilizationCapacityMld;
    private String technology;
    private String frequencyOfMonitoring;
}
