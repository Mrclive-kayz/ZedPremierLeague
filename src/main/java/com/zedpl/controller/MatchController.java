package com.zedpl.controller;

import com.zedpl.dto.MatchEventResponse;
import com.zedpl.dto.MatchResponse;
import com.zedpl.dto.PagedResponse;
import com.zedpl.model.MatchStatus;
import com.zedpl.model.Stream;
import com.zedpl.service.MatchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Public read-only endpoints for matches and match events.
 *
 * <p>Write endpoints (scheduling, results, events) are deferred until
 * Spring Security is implemented.
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * GET /api/matches
     *
     * Supports optional filters:
     *   ?stream=A|B
     *   ?status=SCHEDULED|LIVE|HALF_TIME|FULL_TIME|POSTPONED|CANCELLED
     *   ?teamId=1
     *   ?matchWeek=3
     *   ?from=2026-09-01
     *   ?to=2026-09-30
     *   ?page=0&size=20
     *
     * Default sort: match_date ascending.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<MatchResponse>> listMatches(
            @RequestParam(required = false) Stream stream,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Integer matchWeek,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, MAX_PAGE_SIZE);
        var pageable = PageRequest.of(page, size, Sort.by("matchDate").ascending());
        return ResponseEntity.ok(matchService.findAll(stream, status, teamId, matchWeek, from, to, pageable));
    }

    /**
     * GET /api/matches/{id}
     *
     * Returns a single match, or 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.findById(id));
    }

    /**
     * GET /api/matches/upcoming
     *
     * Returns scheduled fixtures ordered by kickoff time ascending.
     */
    @GetMapping("/upcoming")
    public ResponseEntity<PagedResponse<MatchResponse>> upcomingMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, MAX_PAGE_SIZE);
        return ResponseEntity.ok(matchService.findUpcoming(PageRequest.of(page, size)));
    }

    /**
     * GET /api/matches/recent
     *
     * Returns completed (FULL_TIME) matches, newest first.
     */
    @GetMapping("/recent")
    public ResponseEntity<PagedResponse<MatchResponse>> recentMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, MAX_PAGE_SIZE);
        return ResponseEntity.ok(matchService.findRecent(PageRequest.of(page, size)));
    }

    // ------------------------------------------------------------------ //
    // Match events                                                        //
    // ------------------------------------------------------------------ //

    /**
     * GET /api/matches/{matchId}/events
     *
     * Returns all events for the specified match, ordered chronologically.
     */
    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<MatchEventResponse>> listMatchEvents(
            @PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.findEventsByMatchId(matchId));
    }

    /**
     * GET /api/matches/{matchId}/events/{eventId}
     *
     * Returns a single event, confirming it belongs to the specified match.
     */
    @GetMapping("/{matchId}/events/{eventId}")
    public ResponseEntity<MatchEventResponse> getMatchEvent(
            @PathVariable Long matchId,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(matchService.findEventByMatchAndEventId(matchId, eventId));
    }
}
