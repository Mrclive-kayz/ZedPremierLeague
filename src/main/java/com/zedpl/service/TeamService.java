package com.zedpl.service;

import com.zedpl.dto.PlayerResponse;
import com.zedpl.dto.TeamSummaryResponse;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.model.Player;
import com.zedpl.model.Stream;
import com.zedpl.model.Team;
import com.zedpl.repository.PlayerRepository;
import com.zedpl.repository.TeamRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for team and player queries.
 * All methods are read-only transactions to avoid accidental dirty writes.
 */
@Service
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    /** Returns all teams, optionally filtered by stream, sorted by name. */
    public List<TeamSummaryResponse> findAll(Stream stream) {
        List<Team> teams = (stream != null)
                ? teamRepository.findByStreamOrderByNameAsc(stream)
                : teamRepository.findAll(Sort.by("name"));
        return teams.stream().map(TeamSummaryResponse::from).toList();
    }

    /** Returns a single team by ID, throwing 404 if not found. */
    public TeamSummaryResponse findById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Team", id));
        return TeamSummaryResponse.from(team);
    }

    /**
     * Returns all players belonging to the given team.
     * Validates that the team exists first so the error message is meaningful.
     */
    public List<PlayerResponse> findPlayersByTeamId(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw ResourceNotFoundException.forEntity("Team", teamId);
        }
        return playerRepository.findByTeamId(teamId)
                .stream()
                .map(PlayerResponse::from)
                .toList();
    }
}
