package com.sewagealert.community.service.impl;

import com.sewagealert.community.dto.ArticleRequest;
import com.sewagealert.community.dto.ArticleResponse;
import com.sewagealert.community.model.Article;
import com.sewagealert.community.repository.ArticleRepository;
import com.sewagealert.community.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// ArticleServiceImpl: Core business logic for educational articles — creation, retrieval, updates, and deletion
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    @Override
    // createArticle: Creates a new educational article
    public ArticleResponse createArticle(ArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setAuthorName(request.getAuthorName());

        article = articleRepository.save(article);
        log.info("Article created: {}", article.getTitle());

        return ArticleResponse.fromEntity(article);
    }

    @Override
    // getArticle: Retrieves a single article by its ID
    public ArticleResponse getArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));
        return ArticleResponse.fromEntity(article);
    }

    @Override
    // getAllArticles: Returns all articles in the system
    public List<ArticleResponse> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // getArticlesByCategory: Returns all articles belonging to a given category
    public List<ArticleResponse> getArticlesByCategory(String category) {
        return articleRepository.findByCategory(category).stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    // updateArticle: Updates an existing article's details
    public ArticleResponse updateArticle(Long articleId, ArticleRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setAuthorName(request.getAuthorName());

        article = articleRepository.save(article);
        log.info("Article updated: {}", articleId);

        return ArticleResponse.fromEntity(article);
    }

    @Override
    // deleteArticle: Removes an article by its ID
    public void deleteArticle(Long articleId) {
        articleRepository.deleteById(articleId);
        log.info("Article deleted: {}", articleId);
    }
}
