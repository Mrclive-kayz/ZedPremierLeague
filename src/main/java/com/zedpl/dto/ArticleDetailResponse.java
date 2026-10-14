package com.zedpl.dto;

import com.zedpl.model.Article;
import com.zedpl.model.ArticleCategory;

import java.time.LocalDateTime;

/**
 * Full article detail response including the content body.
 */
public record ArticleDetailResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String content,
        String imageUrl,
        ArticleCategory category,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ArticleDetailResponse from(Article a) {
        return new ArticleDetailResponse(
                a.getId(),
                a.getTitle(),
                a.getSlug(),
                a.getSummary(),
                a.getContent(),
                a.getImageUrl(),
                a.getCategory(),
                a.getPublishedAt(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
