package com.sewagealert.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Educational article create/update request")
public class ArticleRequest {

    @Schema(description = "Article title", example = "How Sewage Treatment Plants Work")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Article body content")
    @NotBlank(message = "Content is required")
    private String content;

    @Schema(description = "Article category", example = "water-conservation")
    @NotBlank(message = "Category is required")
    private String category;

    @Schema(description = "Author name", example = "Dr. Meera Nair")
    @NotBlank(message = "Author name is required")
    private String authorName;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}
