package com.sewagealert.notification.dto;

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
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
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
