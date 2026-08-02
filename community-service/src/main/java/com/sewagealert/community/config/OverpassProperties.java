package com.sewagealert.community.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OverpassProperties: Configuration for the OpenStreetMap Overpass API integration,
 * used to discover lake geometry and location data.
 * <p>
 * Bound from the {@code community.external.overpass} prefix in application.yml.
 * The default bounding box covers the Hyderabad metropolitan area.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "community.external.overpass")
public class OverpassProperties {

    /** Base URL of the public Overpass API instance. */
    private String baseUrl = "https://overpass-api.de";

    /** Bounding box (south, west, north, east) used for the lake query. Defaults to Hyderabad. */
    private Bounds bbox = new Bounds();

    /** Upper bound on the number of lake elements returned to callers. */
    private int maxElements = 200;

    /** Query timeout in seconds sent to Overpass via the [timeout:N] preamble. */
    private int queryTimeoutSeconds = 30;

    /**
     * Bounds: Geographic bounding box in decimal degrees — (south, west) to (north, east).
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Bounds {
        private double south = 17.15;
        private double west = 78.20;
        private double north = 17.65;
        private double east = 78.75;

        public Bounds(double south, double west, double north, double east) {
            this.south = south;
            this.west = west;
            this.north = north;
            this.east = east;
        }
    }
}
