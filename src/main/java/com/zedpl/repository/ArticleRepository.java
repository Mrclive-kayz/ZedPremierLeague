package com.zedpl.repository;

import com.zedpl.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Article> findByPublishedTrueOrderByPublishedAtDesc();

    /** Paginated published articles, ordered by published_at descending. */
    Page<Article> findByPublishedTrue(Pageable pageable);
}
