package com.zedpl.controller;

import com.zedpl.dto.StandingsResponse;
import com.zedpl.model.Stream;
import com.zedpl.service.StandingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoint for league standings.
 *
 * <p>Standings are calculated on the fly from FULL_TIME match data.
 * No separate standings table is used.
 *
 * <p><strong>Season limitation:</strong> the current domain model has no
 * season field. The standings include all FULL_TIME matches ever recorded
 * for the requested stream. A {@code season} query parameter will be added
 * once a migration introduces that field.
 */
@RestController
@RequestMapping("/api/standings")
public class StandingsController {

    private final StandingsService standingsService;

    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    /**
     * GET /api/standings?stream=A
     * GET /api/standings?stream=B
     *
     * Returns the league table for the specified stream.
     * The {@code stream} parameter is required.
     */
    @GetMapping
    public ResponseEntity<StandingsResponse> getStandings(
            @RequestParam Stream stream) {
        return ResponseEntity.ok(standingsService.calculate(stream));
    }
}
