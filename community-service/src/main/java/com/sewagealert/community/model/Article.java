package com.sewagealert.community.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "articles")
// Article: Educational content published by authorities or admins — covers sewage treatment, water conservation, lake restoration, etc.
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // Full article body — supports HTML or Markdown formatting

    @Column(nullable = false)
    private String category;  // e.g., WATER_CONSERVATION, SEWAGE_TREATMENT, LAKE_RESTORATION, GENERAL

    // authorName: Display name of the person or department that published the article
    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "published_at", updatable = false)
    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        publishedAt = LocalDateTime.now();
    }
}
