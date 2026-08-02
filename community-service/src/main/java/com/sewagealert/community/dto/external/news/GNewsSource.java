package com.sewagealert.community.dto.external.news;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GNewsSource: Publication metadata attached to each GNews article.
 */
@Getter
@Setter
@NoArgsConstructor
public class GNewsSource {

    private String name;
    private String url;
}
