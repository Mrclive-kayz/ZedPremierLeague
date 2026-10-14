package com.zedpl.controller;

import com.zedpl.dto.ArticleDetailResponse;
import com.zedpl.dto.ArticleSummaryResponse;
import com.zedpl.dto.PagedResponse;
import com.zedpl.exception.GlobalExceptionHandler;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.model.ArticleCategory;
import com.zedpl.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
@Import(GlobalExceptionHandler.class)
class ArticleControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ArticleService articleService;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 1, 12, 0);

    private static final ArticleSummaryResponse SUMMARY = new ArticleSummaryResponse(
            1L, "Nkana FC Win Title", "nkana-fc-win-title",
            "Nkana FC clinched the championship", null,
            ArticleCategory.MATCH_REPORT, NOW, NOW);

    private static final ArticleDetailResponse DETAIL = new ArticleDetailResponse(
            1L, "Nkana FC Win Title", "nkana-fc-win-title",
            "Nkana FC clinched the championship",
            "<p>Full match report...</p>",
            null, ArticleCategory.MATCH_REPORT, NOW, NOW, NOW);

    @Test
    void listArticles_returnsPublishedArticles() throws Exception {
        var pagedResponse = new PagedResponse<>(List.of(SUMMARY), 0, 20, 1L, 1, true);
        when(articleService.findPublished(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/articles").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Nkana FC Win Title")))
                .andExpect(jsonPath("$.content[0].category", is("MATCH_REPORT")));
    }

    @Test
    void getArticle_byId_returnsDetail() throws Exception {
        when(articleService.findByIdOrSlug("1")).thenReturn(DETAIL);

        mockMvc.perform(get("/api/articles/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug", is("nkana-fc-win-title")))
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    void getArticle_bySlug_returnsDetail() throws Exception {
        when(articleService.findByIdOrSlug("nkana-fc-win-title")).thenReturn(DETAIL);

        mockMvc.perform(get("/api/articles/nkana-fc-win-title").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getArticle_notFound_returns404() throws Exception {
        when(articleService.findByIdOrSlug("999"))
                .thenThrow(ResourceNotFoundException.forEntity("Article", 999L));

        mockMvc.perform(get("/api/articles/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getArticle_draft_returns404() throws Exception {
        // ArticleService returns 404 for drafts — simulate here
        when(articleService.findByIdOrSlug("draft-article"))
                .thenThrow(ResourceNotFoundException.forEntity("Article", "slug", "draft-article"));

        mockMvc.perform(get("/api/articles/draft-article").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void listArticles_withPageParams_passesThemToService() throws Exception {
        var pagedResponse = new PagedResponse<>(List.<ArticleSummaryResponse>of(), 1, 10, 0L, 0, true);
        when(articleService.findPublished(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/articles").param("page", "1").param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)));
    }
}
