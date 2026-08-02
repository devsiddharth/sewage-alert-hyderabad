package com.sewagealert.community.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NewsApiProperties: Configuration for the external news feed integration.
 * <p>
 * The current provider is the GNews API (https://gnews.io) — a free public news API.
 * Bound from the {@code community.external.news-api} prefix in application.yml
 * (replaces scattered {@code @Value} annotations).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "community.external.news-api")
public class NewsApiProperties {

    /** Base URL of the news provider (defaults to GNews API). */
    private String baseUrl = "https://gnews.io";

    /** GNews API token. Recommended: set via the GNEWS_API_KEY environment variable. */
    private String apiKey;

    /** ISO 639-1 language code used for news filtering (default: English). */
    private String language = "en";

    /** ISO 3166-1 alpha-2 country code used for news filtering (default: India). */
    private String country = "in";

    /** Default number of articles to return per request when pageSize is not supplied. */
    private int defaultPageSize = 10;

    /** Hard upper bound for pageSize — GNews free tier caps this at 10 per request. */
    private int maxPageSize = 10;

    /** Keyword used when no explicit keyword query parameter is supplied. */
    private String defaultKeyword = "water";
}
