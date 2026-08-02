package com.sewagealert.community.dto;

import com.sewagealert.community.model.Article;
import java.time.LocalDateTime;

public class ArticleResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String authorName;
    private LocalDateTime publishedAt;

    public ArticleResponse() {}

    public static ArticleResponse fromEntity(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setContent(article.getContent());
        response.setCategory(article.getCategory());
        response.setAuthorName(article.getAuthorName());
        response.setPublishedAt(article.getPublishedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
