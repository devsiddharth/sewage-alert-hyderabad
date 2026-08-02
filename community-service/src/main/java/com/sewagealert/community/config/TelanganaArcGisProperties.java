package com.sewagealert.community.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TelanganaArcGisProperties: Configuration for the Telangana Government ArcGIS REST service
 * (TGRAC), which exposes a public, key-less STP (sewage treatment plant) location layer.
 * <p>
 * Layer verified live: MapServer/7 (STP_Locations) of TCUR_Telangana_Core_Urban_Region_V2,
 * containing plant name, lat/lon, commissioning year, operational status, capacity (MLD),
 * technology, and monitoring frequency.
 * <p>
 * Bound from the {@code community.external.telangana-arcgis} prefix in application.yml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "community.external.telangana-arcgis")
public class TelanganaArcGisProperties {

    /** Base URL of the Telangana government ArcGIS REST service. */
    private String baseUrl = "https://tgrac.telangana.gov.in";

    /** Maximum number of STP records to request per query (server cap: 1000). */
    private int maxRecords = 1000;
}
