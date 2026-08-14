package com.sewagealert.community.controller;

import com.sewagealert.community.dto.ApiResponse;
import com.sewagealert.community.dto.ArticleRequest;
import com.sewagealert.community.dto.ArticleResponse;
import com.sewagealert.community.dto.external.news.ArticleFeedItem;
import com.sewagealert.community.service.ArticleExternalService;
import com.sewagealert.community.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "Articles", description = "Educational articles about sewage treatment, water conservation and lake restoration")
// ArticleController: Manages educational articles about sewage treatment, water conservation, lake restoration, etc.
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleExternalService articleExternalService;

    public ArticleController(ArticleService articleService, ArticleExternalService articleExternalService) {
        this.articleService = articleService;
        this.articleExternalService = articleExternalService;
    }

    @PostMapping
    @Operation(
            summary = "Create an article",
            description = "Creates an educational article."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Article created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(@Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", articleService.createArticle(request)));
    }

    @GetMapping
    @Operation(
            summary = "List articles",
            description = "Returns all articles, optionally filtered by category."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Articles retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getAllArticles(
            @Parameter(description = "Optional category filter", example = "water-conservation")
            @RequestParam(required = false) String category) {
        if (category != null) {
            return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully",
                    articleService.getArticlesByCategory(category)));
        }
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully", articleService.getAllArticles()));
    }

    // getLatestArticles: Fetches fresh articles from the external news provider (GNews)
    @GetMapping("/latest")
    @Operation(
            summary = "Fetch the latest articles from an external news provider",
            description = "Returns fresh articles from the GNews external API. A pageSize of 0 uses the "
                    + "configured default (capped at 10 on the free tier)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest articles retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "External news provider error")
    })
    public ResponseEntity<ApiResponse<List<ArticleFeedItem>>> getLatestArticles(
            @Parameter(description = "Optional keyword filter", example = "sewage") @RequestParam(required = false) String keyword,
            @Parameter(description = "Optional category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Page number (1-based)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size — 0 uses the configured default (max 10)", example = "0") @RequestParam(defaultValue = "0") int pageSize) {  // 0 = use configured default
        return ResponseEntity.ok(ApiResponse.success("Latest articles retrieved successfully",
                articleExternalService.getLatestArticles(keyword, category, page, pageSize)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get an article by id",
            description = "Returns a single educational article."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Article retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Article not found")
    })
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticle(
            @Parameter(description = "Article id", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Article retrieved successfully", articleService.getArticle(id)));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an article",
            description = "Updates an existing educational article."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Article updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Article not found")
    })
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @Parameter(description = "Article id", example = "1") @PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", articleService.updateArticle(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an article",
            description = "Deletes an educational article."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Article deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Article not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @Parameter(description = "Article id", example = "1") @PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted successfully", null));
    }
}
