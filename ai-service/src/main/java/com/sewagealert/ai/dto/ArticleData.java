package com.sewagealert.ai.dto;

import lombok.Data;

/**
 * ArticleData: Lightweight projection of article data for AI context building.
 */
@Data
public class ArticleData {
    private Long id;
    private String title;
    private String category;
    private String authorName;
    private String publishedAt;
    /** Content is truncated to first 500 chars in context to avoid token overflow. */
    private String contentPreview;
}
