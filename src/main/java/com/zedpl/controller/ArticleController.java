package com.zedpl.controller;

import com.zedpl.dto.ArticleDetailResponse;
import com.zedpl.dto.ArticleSummaryResponse;
import com.zedpl.dto.PagedResponse;
import com.zedpl.service.ArticleService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public read-only endpoints for news articles.
 *
 * <p>Only published articles are visible. Drafts are hidden behind 404.
 * Write endpoints (create / update / publish) are deferred until Spring
 * Security is implemented.
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private static final int MAX_PAGE_SIZE = 50;

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * GET /api/articles
     * GET /api/articles?page=0&size=20
     *
     * Returns published articles, newest first, paginated.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ArticleSummaryResponse>> listArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        size = Math.min(size, MAX_PAGE_SIZE);
        return ResponseEntity.ok(articleService.findPublished(PageRequest.of(page, size)));
    }

    /**
     * GET /api/articles/{identifier}
     *
     * Accepts either a numeric ID or a slug.
     * Returns 404 for drafts and non-existent articles.
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<ArticleDetailResponse> getArticle(
            @PathVariable String identifier) {
        return ResponseEntity.ok(articleService.findByIdOrSlug(identifier));
    }
}
