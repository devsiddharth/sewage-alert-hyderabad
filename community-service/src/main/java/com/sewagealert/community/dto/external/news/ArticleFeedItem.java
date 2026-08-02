package com.sewagealert.community.dto.external.news;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ArticleFeedItem: The sanitized article representation returned to API callers.
 * <p>
 * Only useful fields are exposed — raw GNews responses are never surfaced.
 * {@code author} is always null because GNews does not provide author metadata.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArticleFeedItem {

    private String title;
    private String description;
    private String imageUrl;
    private String articleUrl;
    private String source;
    private String author;
    private String publishedAt;
}
