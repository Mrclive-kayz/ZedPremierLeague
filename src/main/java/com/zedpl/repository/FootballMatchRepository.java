package com.zedpl.repository;

import com.zedpl.model.FootballMatch;
import com.zedpl.model.MatchStatus;
import com.zedpl.model.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FootballMatchRepository extends JpaRepository<FootballMatch, Long> {

    List<FootballMatch> findByStreamOrderByMatchDateAsc(Stream stream);

    List<FootballMatch> findByStatusOrderByMatchDateAsc(MatchStatus status);

    /** Input for the future standings calculation, e.g. (A, FULL_TIME). */
    List<FootballMatch> findByStreamAndStatus(Stream stream, MatchStatus status);

    List<FootballMatch> findAllByOrderByMatchDateAsc();

    // ------------------------------------------------------------------ //
    // Paginated queries used by the REST API                              //
    // ------------------------------------------------------------------ //

    /**
     * Flexible filter: all parameters are optional. A null value for stream,
     * teamId, status, from, or to means "no constraint on that field".
     *
     * <p>Using a single JPQL query with IS NULL OR … guards avoids writing
     * combinatorial derived-method names.
     */
    @Query("""
            SELECT m FROM FootballMatch m
            WHERE (:stream IS NULL OR m.stream = :stream)
              AND (:status IS NULL OR m.status = :status)
              AND (:teamId IS NULL
                   OR m.homeTeam.id = :teamId
                   OR m.awayTeam.id = :teamId)
              AND (:matchWeek IS NULL OR m.matchWeek = :matchWeek)
              AND (:from IS NULL OR m.matchDate >= :from)
              AND (:to IS NULL OR m.matchDate <= :to)
            """)
    Page<FootballMatch> findByFilters(
            @Param("stream") Stream stream,
            @Param("status") MatchStatus status,
            @Param("teamId") Long teamId,
            @Param("matchWeek") Integer matchWeek,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    /** Upcoming: status = SCHEDULED, ordered by match date ascending. */
    Page<FootballMatch> findByStatusOrderByMatchDateAsc(MatchStatus status, Pageable pageable);

    /** Recently played: status = FULL_TIME, ordered by match date descending. */
    Page<FootballMatch> findByStatusOrderByMatchDateDesc(MatchStatus status, Pageable pageable);
}
