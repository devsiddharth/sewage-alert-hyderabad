package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.ArticleRequest;
import com.sewagealert.community.dto.ArticleResponse;
import com.sewagealert.community.dto.external.news.ArticleFeedItem;
import com.sewagealert.community.service.ArticleExternalService;
import com.sewagealert.community.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
// ArticleController: Manages educational articles about sewage treatment, water conservation, lake restoration, etc.
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleExternalService articleExternalService;

    public ArticleController(ArticleService articleService, ArticleExternalService articleExternalService) {
        this.articleService = articleService;
        this.articleExternalService = articleExternalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(@Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", articleService.createArticle(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getAllArticles(
            @RequestParam(required = false) String category) {
        if (category != null) {
            return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully",
                    articleService.getArticlesByCategory(category)));
        }
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully", articleService.getAllArticles()));
    }

    // getLatestArticles: Fetches fresh articles from the external news provider (GNews)
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ArticleFeedItem>>> getLatestArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "0") int pageSize) {  // 0 = use configured default
        return ResponseEntity.ok(ApiResponse.success("Latest articles retrieved successfully",
                articleExternalService.getLatestArticles(keyword, category, page, pageSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Article retrieved successfully", articleService.getArticle(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", articleService.updateArticle(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted successfully", null));
    }
}
