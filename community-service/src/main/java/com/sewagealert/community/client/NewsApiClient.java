package com.sewagealert.community.client;

import com.sewagealert.community.dto.external.news.GNewsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * NewsApiClient: Declarative OpenFeign client for the GNews API (https://gnews.io).
 * <p>
 * GNews is an external public API — not registered in Eureka — so the base URL comes from
 * {@code community.external.news-api.base-url} configuration and load balancing is not applied.
 */
@FeignClient(name = "news-api", url = "${community.external.news-api.base-url}")
public interface NewsApiClient {

    /**
     * search: Full-text article search.
     *
     * @param keyword  search terms (required by GNews)
     * @param apiKey   GNews API token (query param name is "token")
     * @param language ISO 639-1 language code, e.g. "en"
     * @param country  ISO 3166-1 alpha-2 country code, e.g. "in"
     * @param category optional news category (general, world, nation, business, technology, entertainment, sports, science, health)
     * @param pageSize max articles per request (free tier caps at 10)
     * @param page     1-based page number
     */
    @GetMapping("/api/v4/search")
    GNewsResponse search(@RequestParam("q") String keyword,
                         @RequestParam("token") String apiKey,
                         @RequestParam(value = "lang", required = false) String language,
                         @RequestParam(value = "country", required = false) String country,
                         @RequestParam(value = "category", required = false) String category,
                         @RequestParam(value = "max", defaultValue = "10") int pageSize,
                         @RequestParam(value = "page", defaultValue = "1") int page);
}
