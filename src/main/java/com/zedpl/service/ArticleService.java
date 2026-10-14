package com.zedpl.service;

import com.zedpl.dto.ArticleDetailResponse;
import com.zedpl.dto.ArticleSummaryResponse;
import com.zedpl.dto.PagedResponse;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.model.Article;
import com.zedpl.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for article queries.
 * Only published articles are surfaced through public endpoints.
 */
@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /**
     * Returns published articles newest-first, paginated.
     */
    public PagedResponse<ArticleSummaryResponse> findPublished(Pageable pageable) {
        // Always sort by published_at descending regardless of caller-supplied sort.
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("publishedAt").descending());
        Page<Article> page = articleRepository.findByPublishedTrue(sorted);
        return PagedResponse.from(page.map(ArticleSummaryResponse::from));
    }

    /**
     * Returns a published article by its ID or slug.
     * The identifier string is tried as a numeric ID first; if parsing fails
     * it is treated as a slug. Drafts (published = false) are not surfaced.
     *
     * @param identifier numeric ID as string, or slug
     * @throws ResourceNotFoundException when no published article matches
     */
    public ArticleDetailResponse findByIdOrSlug(String identifier) {
        Article article;
        try {
            long id = Long.parseLong(identifier);
            article = articleRepository.findById(id)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("Article", id));
        } catch (NumberFormatException e) {
            article = articleRepository.findBySlug(identifier)
                    .orElseThrow(() -> ResourceNotFoundException.forEntity(
                            "Article", "slug", identifier));
        }

        if (!article.isPublished()) {
            // Return 404 rather than 403 so draft existence is not revealed.
            throw ResourceNotFoundException.forEntity("Article",
                    isNumeric(identifier) ? Long.parseLong(identifier) : 0L);
        }
        return ArticleDetailResponse.from(article);
    }

    private boolean isNumeric(String s) {
        try { Long.parseLong(s); return true; } catch (NumberFormatException e) { return false; }
    }
}
