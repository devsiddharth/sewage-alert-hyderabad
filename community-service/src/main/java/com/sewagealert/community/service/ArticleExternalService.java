package com.sewagealert.community.service;

import com.sewagealert.community.dto.external.news.ArticleFeedItem;

import java.util.List;

/**
 * ArticleExternalService: Fetches fresh news articles from an external news provider
 * (currently GNews). Kept separate from the local Article CRUD service.
 */
public interface ArticleExternalService {

    /**
     * getLatestArticles: Retrieves the latest articles matching the given keyword/category.
     *
     * @param keyword  search keyword (defaults to "water" when blank)
     * @param category optional news category, e.g. "science", "health"
     * @param page     1-based page number
     * @param pageSize number of articles per page (bounded by configuration)
     */
    List<ArticleFeedItem> getLatestArticles(String keyword, String category, int page, int pageSize);
}
