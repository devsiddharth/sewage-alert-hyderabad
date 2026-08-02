package com.sewagealert.community.dto.external.news;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * GNewsResponse: Raw response envelope from the GNews search endpoint.
 * Used only for deserialization — never exposed to API callers.
 */
@Getter
@Setter
@NoArgsConstructor
public class GNewsResponse {

    private long totalArticles;
    private List<GNewsArticle> articles;
}
