package com.sewagealert.community.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GooglePlacesProperties: Configuration for the Google Places API integration
 * (legacy Text Search + Place Details endpoints), used to discover NGOs by city/keyword.
 * <p>
 * Bound from the {@code community.external.google-places} prefix in application.yml.
 * Requires a Google Cloud API key with the Places API enabled and a linked billing account.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "community.external.google-places")
public class GooglePlacesProperties {

    /** Base URL of the Google Maps Platform API. */
    private String baseUrl = "https://maps.googleapis.com";

    /** Google Cloud API key. Recommended: set via the GOOGLE_PLACES_API_KEY environment variable. */
    private String apiKey;

    /** Maximum number of NGO results to enrich (each one triggers a Place Details call). */
    private int maxResults = 5;
}
