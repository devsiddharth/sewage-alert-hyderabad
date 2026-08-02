package com.sewagealert.community.service.impl;

import com.sewagealert.community.client.NewsApiClient;
import com.sewagealert.community.config.NewsApiProperties;
import com.sewagealert.community.dto.external.news.ArticleFeedItem;
import com.sewagealert.community.dto.external.news.GNewsArticle;
import com.sewagealert.community.dto.external.news.GNewsResponse;
import com.sewagealert.community.exception.NewsApiException;
import com.sewagealert.community.service.ArticleExternalService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ArticleExternalServiceImpl: Orchestrates calls to the GNews API and maps responses
 * into sanitized {@link ArticleFeedItem}s. Raw GNews payloads never leave this class.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleExternalServiceImpl implements ArticleExternalService {

    private final NewsApiClient newsApiClient;
    private final NewsApiProperties newsApiProperties;

    @Override
    public List<ArticleFeedItem> getLatestArticles(String keyword, String category, int page, int pageSize) {
        String apiKey = newsApiProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new NewsApiException("News API key is not configured. Set community.external.news-api.api-key or the GNEWS_API_KEY environment variable.");
        }

        String resolvedKeyword = (keyword == null || keyword.isBlank())
                ? newsApiProperties.getDefaultKeyword() : keyword.trim();
        int resolvedPage = Math.max(page, 1);
        int resolvedPageSize = pageSize > 0
                ? Math.min(pageSize, newsApiProperties.getMaxPageSize())
                : newsApiProperties.getDefaultPageSize();

        log.info("Fetching latest articles from GNews — keyword='{}', category={}, page={}, pageSize={}",
                resolvedKeyword, category, resolvedPage, resolvedPageSize);

        GNewsResponse response;
        try {
            response = newsApiClient.search(
                    resolvedKeyword, apiKey,
                    newsApiProperties.getLanguage(), newsApiProperties.getCountry(),
                    category, resolvedPageSize, resolvedPage);
        } catch (FeignException ex) {
            log.error("GNews request failed — status={}, error={}", ex.status(), ex.getMessage());
            throw new NewsApiException("Failed to fetch news articles from the news provider", ex);
        }

        if (response == null || response.getArticles() == null) {
            log.warn("GNews returned an empty response for keyword='{}'", resolvedKeyword);
            return List.of();
        }

        log.info("Successfully fetched {} articles from GNews", response.getArticles().size());
        return response.getArticles().stream()
                .map(this::toFeedItem)
                .collect(Collectors.toList());
    }

    private ArticleFeedItem toFeedItem(GNewsArticle article) {
        ArticleFeedItem item = new ArticleFeedItem();
        item.setTitle(article.getTitle());
        item.setDescription(article.getDescription());
        item.setImageUrl(article.getImage());
        item.setArticleUrl(article.getUrl());
        item.setSource(article.getSource() != null ? article.getSource().getName() : null);
        item.setAuthor(null); // GNews does not expose author metadata
        item.setPublishedAt(article.getPublishedAt());
        return item;
    }
}
