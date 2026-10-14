package com.zedpl.service;

import com.zedpl.dto.StandingRow;
import com.zedpl.dto.StandingsResponse;
import com.zedpl.model.FootballMatch;
import com.zedpl.model.MatchStatus;
import com.zedpl.model.Stream;
import com.zedpl.model.Team;
import com.zedpl.repository.FootballMatchRepository;
import com.zedpl.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Calculates league standings from completed (FULL_TIME) matches.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Load every team in the requested stream — this ensures teams with
 *       zero completed matches still appear in the table.</li>
 *   <li>Load every FULL_TIME match in the stream.</li>
 *   <li>For each match, update the accumulators for both the home team and
 *       the away team.</li>
 *   <li>Sort by points DESC, goal difference DESC, goals-for DESC,
 *       then by team name ASC as a deterministic final tie-breaker.</li>
 *   <li>Assign sequential 1-based positions after sorting.</li>
 * </ol>
 *
 * <h2>Season support</h2>
 * The current domain model has no season field on matches or teams.
 * Therefore, standings reflect all FULL_TIME matches ever recorded for the
 * stream. A future migration can add a {@code season} column and a
 * {@code season} filter parameter to this service.
 *
 * <h2>Score validation note</h2>
 * This service trusts the scores stored in the database. Invalid scores
 * (negative values) are prevented by the DB check constraint
 * {@code ck_matches_scores} and the JPA {@code @Min(0)} annotations.
 */
@Service
@Transactional(readOnly = true)
public class StandingsService {

    private final TeamRepository teamRepository;
    private final FootballMatchRepository matchRepository;

    public StandingsService(TeamRepository teamRepository,
                            FootballMatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Builds and returns the full standings table for the given stream.
     *
     * @param stream Stream.A or Stream.B — required, must not be null
     */
    public StandingsResponse calculate(Stream stream) {
        Objects.requireNonNull(stream, "stream must not be null");

        // Step 1: seed one accumulator per team in this stream
        Map<Long, TeamStats> statsMap = new LinkedHashMap<>();
        for (Team team : teamRepository.findByStreamOrderByNameAsc(stream)) {
            statsMap.put(team.getId(), new TeamStats(team));
        }

        // Step 2: accumulate results from FULL_TIME matches
        for (FootballMatch m : matchRepository.findByStreamAndStatus(stream, MatchStatus.FULL_TIME)) {
            Long homeId = m.getHomeTeam().getId();
            Long awayId = m.getAwayTeam().getId();

            TeamStats home = statsMap.get(homeId);
            TeamStats away = statsMap.get(awayId);

            // Guard: if a team was removed from the stream after the match
            // was played, skip rather than NPE.
            if (home == null || away == null) continue;

            int homeGoals = m.getHomeScore();
            int awayGoals = m.getAwayScore();

            home.played++;
            away.played++;
            home.goalsFor += homeGoals;
            home.goalsAgainst += awayGoals;
            away.goalsFor += awayGoals;
            away.goalsAgainst += homeGoals;

            if (homeGoals > awayGoals) {
                home.won++;
                home.points += 3;
                away.lost++;
            } else if (homeGoals < awayGoals) {
                away.won++;
                away.points += 3;
                home.lost++;
            } else {
                home.drawn++;
                home.points++;
                away.drawn++;
                away.points++;
            }
        }

        // Step 3: sort
        List<TeamStats> sorted = new ArrayList<>(statsMap.values());
        sorted.sort(Comparator
                .<TeamStats, Integer>comparing(s -> -s.points)
                .thenComparingInt(s -> -s.goalDifference())
                .thenComparingInt(s -> -s.goalsFor)
                .thenComparing(s -> s.team.getName()));

        // Step 4: build DTOs with 1-based position
        List<StandingRow> rows = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            rows.add(sorted.get(i).toRow(i + 1, stream));
        }

        return new StandingsResponse(stream, rows);
    }

    // ------------------------------------------------------------------ //
    // Internal accumulator                                                //
    // ------------------------------------------------------------------ //

    private static final class TeamStats {
        final Team team;
        int played, won, drawn, lost, goalsFor, goalsAgainst, points;

        TeamStats(Team team) { this.team = team; }

        int goalDifference() { return goalsFor - goalsAgainst; }

        StandingRow toRow(int position, Stream stream) {
            return new StandingRow(
                    position,
                    team.getId(),
                    team.getName(),
                    team.getShortName(),
                    team.getLogoUrl(),
                    stream,
                    played, won, drawn, lost,
                    goalsFor, goalsAgainst,
                    goalDifference(),
                    points
            );
        }
    }
}
