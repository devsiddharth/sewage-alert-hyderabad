package com.sewagealert.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// PagedResponse<T>: Generic pagination wrapper used by list endpoints — newest-first ordering
// is applied at the query level (Sort.by("createdAt").descending()).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic pagination wrapper used by list endpoints (newest-first)")
public class PagedResponse<T> {

    @Schema(description = "Page of items")
    private List<T> content;

    @Schema(description = "Zero-based page number", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of elements across all pages", example = "57")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    // fromPage: Maps a Spring Data Page into this lightweight DTO using the given element mapper
    public static <E, T> PagedResponse<T> fromPage(Page<E> page, Function<E, T> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
