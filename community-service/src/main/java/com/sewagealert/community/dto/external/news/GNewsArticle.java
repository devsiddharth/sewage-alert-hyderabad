package com.sewagealert.community.dto.external.news;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GNewsArticle: A single article as returned by the GNews API.
 * Note: GNews does not expose an author field.
 */
@Getter
@Setter
@NoArgsConstructor
public class GNewsArticle {

    private String title;
    private String description;
    private String content;
    private String url;
    private String image;
    private String publishedAt;
    private GNewsSource source;
}
