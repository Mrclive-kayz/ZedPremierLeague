package com.zedpl.dto;

import com.zedpl.model.Article;
import com.zedpl.model.ArticleCategory;

import java.time.LocalDateTime;

/**
 * Article list item — omits the full content body to keep list responses compact.
 */
public record ArticleSummaryResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String imageUrl,
        ArticleCategory category,
        LocalDateTime publishedAt,
        LocalDateTime createdAt
) {
    public static ArticleSummaryResponse from(Article a) {
        return new ArticleSummaryResponse(
                a.getId(),
                a.getTitle(),
                a.getSlug(),
                a.getSummary(),
                a.getImageUrl(),
                a.getCategory(),
                a.getPublishedAt(),
                a.getCreatedAt()
        );
    }
}
