package com.zedpl.service;

import com.zedpl.dto.PagedResponse;
import com.zedpl.dto.PlayerResponse;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.model.Player;
import com.zedpl.model.PlayerPosition;
import com.zedpl.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for player queries.
 * Filtering by team and position is done in-memory after loading the page
 * because the existing PlayerRepository only has findByTeamId.
 * When filter combinations are needed frequently, a JPA Specification or
 * a @Query with optional parameters should be added to PlayerRepository.
 */
@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Returns a paginated, optionally-filtered list of players.
     *
     * <p>When both teamId and position are absent the full repository page is used
     * directly. When either filter is present we fall back to loading all matching
     * players and slicing them, which is acceptable for the dataset sizes expected
     * in a league application (hundreds, not millions of players).
     */
    public PagedResponse<PlayerResponse> findAll(Long teamId, PlayerPosition position, Pageable pageable) {
        if (teamId == null && position == null) {
            Page<Player> page = playerRepository.findAll(pageable);
            return PagedResponse.from(page.map(PlayerResponse::from));
        }

        // Load filtered candidates then apply manual pagination
        List<Player> filtered;
        if (teamId != null) {
            filtered = playerRepository.findByTeamId(teamId);
        } else {
            filtered = playerRepository.findAll();
        }

        if (position != null) {
            filtered = filtered.stream()
                    .filter(p -> p.getPosition() == position)
                    .toList();
        }

        // Sort by last name, then first name for deterministic ordering
        filtered = filtered.stream()
                .sorted(java.util.Comparator
                        .comparing(Player::getLastName)
                        .thenComparing(Player::getFirstName))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Player> pageContent = (start >= filtered.size()) ? List.of() : filtered.subList(start, end);

        Page<Player> page = new PageImpl<>(pageContent, pageable, filtered.size());
        return PagedResponse.from(page.map(PlayerResponse::from));
    }

    /** Returns a single player by ID, throwing 404 if not found. */
    public PlayerResponse findById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Player", id));
        return PlayerResponse.from(player);
    }
}
