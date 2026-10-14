package com.zedpl.controller;

import com.zedpl.dto.PagedResponse;
import com.zedpl.dto.PlayerResponse;
import com.zedpl.model.PlayerPosition;
import com.zedpl.service.PlayerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public read-only endpoints for players.
 *
 * <p>Supports optional filtering by team and position, with pagination.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * GET /api/players
     * GET /api/players?teamId=1&position=GOALKEEPER&page=0&size=20
     *
     * Returns a paginated list of players with optional filtering.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<PlayerResponse>> listPlayers(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) PlayerPosition position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(playerService.findAll(teamId, position, pageable));
    }

    /**
     * GET /api/players/{id}
     *
     * Returns a single player, or 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.findById(id));
    }
}
