package com.sewagealert.community.dto.external.telangana;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ArcGisAttributes: Attribute fields of an STP feature.
 * Field names mirror the Telangana ArcGIS layer schema exactly via @JsonProperty
 * (e.g. "Location_of_STP", "Installed_Capacity__MLD_").
 */
@Getter
@Setter
@NoArgsConstructor
public class ArcGisAttributes {

    @JsonProperty("OBJECTID")
    private Long objectId;

    @JsonProperty("Sl_No")
    private Double slNo;

    @JsonProperty("Location_of_STP")
    private String locationOfStp;

    @JsonProperty("Latitude")
    private Double latitude;

    @JsonProperty("Longitude")
    private Double longitude;

    @JsonProperty("Year_of_commissioning")
    private Double yearOfCommissioning;

    @JsonProperty("Operational_Status")
    private String operationalStatus;

    @JsonProperty("Installed_Capacity__MLD_")
    private Double installedCapacityMld;

    @JsonProperty("Utilization_capacity__MLD_")
    private Double utilizationCapacityMld;

    @JsonProperty("Technology")
    private String technology;

    @JsonProperty("Frequency_of_monitoring")
    private String frequencyOfMonitoring;
}
