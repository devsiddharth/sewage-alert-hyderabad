package com.sewagealert.community.service;

import com.sewagealert.community.dto.ArticleRequest;
import com.sewagealert.community.dto.ArticleResponse;

import java.util.List;

// ArticleService: Business logic for educational articles about sewage treatment, water conservation, and lake restoration
public interface ArticleService {

    // createArticle: Creates a new educational article
    ArticleResponse createArticle(ArticleRequest request);

    // getArticle: Retrieves a single article by its ID
    ArticleResponse getArticle(Long articleId);

    // getAllArticles: Returns all articles in the system
    List<ArticleResponse> getAllArticles();

    // getArticlesByCategory: Returns all articles belonging to a given category
    List<ArticleResponse> getArticlesByCategory(String category);

    // updateArticle: Updates an existing article's details
    ArticleResponse updateArticle(Long articleId, ArticleRequest request);

    // deleteArticle: Removes an article by its ID
    void deleteArticle(Long articleId);
}
