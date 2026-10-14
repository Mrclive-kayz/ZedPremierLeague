package com.zedpl.controller;

import com.zedpl.dto.PlayerResponse;
import com.zedpl.dto.TeamSummaryResponse;
import com.zedpl.model.Stream;
import com.zedpl.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public read-only endpoints for teams and their rosters.
 *
 * <p>Write endpoints (POST/PUT/DELETE) are deferred until Spring Security
 * and role-based authentication are implemented.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * GET /api/teams
     * GET /api/teams?stream=A
     * GET /api/teams?stream=B
     *
     * Returns all teams, optionally filtered by stream.
     */
    @GetMapping
    public ResponseEntity<List<TeamSummaryResponse>> listTeams(
            @RequestParam(required = false) Stream stream) {
        return ResponseEntity.ok(teamService.findAll(stream));
    }

    /**
     * GET /api/teams/{id}
     *
     * Returns a single team, or 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamSummaryResponse> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.findById(id));
    }

    /**
     * GET /api/teams/{teamId}/players
     *
     * Returns the squad for a given team, or 404 if the team does not exist.
     */
    @GetMapping("/{teamId}/players")
    public ResponseEntity<List<PlayerResponse>> getTeamPlayers(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.findPlayersByTeamId(teamId));
    }
}
