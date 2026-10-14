package com.zedpl.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic pagination envelope returned by collection endpoints.
 *
 * @param content      the items on the current page
 * @param page         zero-based current page number
 * @param size         requested page size
 * @param totalElements total number of matching items across all pages
 * @param totalPages   total number of pages
 * @param last         true when this is the last page
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
