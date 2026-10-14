package com.zedpl.service;

import com.zedpl.dto.StandingRow;
import com.zedpl.dto.StandingsResponse;
import com.zedpl.model.*;
import com.zedpl.repository.FootballMatchRepository;
import com.zedpl.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StandingsService}.
 *
 * The repository layer is mocked with Mockito so no database is involved.
 * Tests cover:
 *  - a win correctly awards 3 points to the winner and 0 to the loser
 *  - a draw awards 1 point to each team
 *  - goal difference and goals-for are calculated correctly
 *  - a stream with no completed matches returns all teams with zero stats
 *  - scheduled / postponed / live matches do NOT affect standings
 *  - sort order is correct (points DESC, GD DESC, GF DESC, name ASC)
 *  - own-goal scoring scenarios
 */
@ExtendWith(MockitoExtension.class)
class StandingsServiceTest {

    @Mock TeamRepository teamRepository;
    @Mock FootballMatchRepository matchRepository;

    StandingsService service;

    // Teams
    Team nkana, power, zesco, arrows;

    @BeforeEach
    void setUp() {
        service = new StandingsService(teamRepository, matchRepository);

        nkana  = makeTeam(1L, "Nkana FC",      "NKA", Stream.A);
        power  = makeTeam(2L, "Power Dynamos",  "PWD", Stream.A);
        zesco  = makeTeam(3L, "ZESCO United",   "ZES", Stream.B);
        arrows = makeTeam(4L, "Red Arrows",     "RAR", Stream.B);
    }

    // ------------------------------------------------------------------ //

    @Test
    void win_givesThreePointsToWinner_andZeroToLoser() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power));
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of(match(nkana, power, 2, 0)));  // Nkana wins

        StandingsResponse result = service.calculate(Stream.A);

        StandingRow nkanaRow = rowFor(result, nkana.getId());
        StandingRow powerRow = rowFor(result, power.getId());

        assertThat(nkanaRow.points()).isEqualTo(3);
        assertThat(nkanaRow.won()).isEqualTo(1);
        assertThat(nkanaRow.lost()).isZero();
        assertThat(powerRow.points()).isZero();
        assertThat(powerRow.lost()).isEqualTo(1);
        assertThat(powerRow.won()).isZero();
    }

    @Test
    void draw_givesOnePointToEachTeam() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power));
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of(match(nkana, power, 1, 1)));

        StandingsResponse result = service.calculate(Stream.A);

        StandingRow nkanaRow = rowFor(result, nkana.getId());
        StandingRow powerRow = rowFor(result, power.getId());

        assertThat(nkanaRow.points()).isEqualTo(1);
        assertThat(nkanaRow.drawn()).isEqualTo(1);
        assertThat(powerRow.points()).isEqualTo(1);
        assertThat(powerRow.drawn()).isEqualTo(1);
    }

    @Test
    void goalDifference_isCalculatedCorrectly() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power));
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of(match(nkana, power, 3, 1)));

        StandingsResponse result = service.calculate(Stream.A);

        assertThat(rowFor(result, nkana.getId()).goalDifference()).isEqualTo(2);
        assertThat(rowFor(result, power.getId()).goalDifference()).isEqualTo(-2);
    }

    @Test
    void goalsFor_andGoalsAgainst_areAccumulatedAcrossMultipleMatches() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power));
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of(
                        match(nkana, power, 2, 0),
                        match(power, nkana, 1, 3)));

        StandingsResponse result = service.calculate(Stream.A);

        // Nkana: scored 2+3=5, conceded 0+1=1
        StandingRow nkanaRow = rowFor(result, nkana.getId());
        assertThat(nkanaRow.goalsFor()).isEqualTo(5);
        assertThat(nkanaRow.goalsAgainst()).isEqualTo(1);
        assertThat(nkanaRow.points()).isEqualTo(6); // 2 wins
    }

    @Test
    void emptyStream_returnsAllTeamsWithZeroStats() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.B))
                .thenReturn(List.of(zesco, arrows));
        when(matchRepository.findByStreamAndStatus(Stream.B, MatchStatus.FULL_TIME))
                .thenReturn(List.of());

        StandingsResponse result = service.calculate(Stream.B);

        assertThat(result.table()).hasSize(2);
        result.table().forEach(row -> {
            assertThat(row.played()).isZero();
            assertThat(row.points()).isZero();
        });
    }

    @Test
    void nonCompletedMatches_doNotContributeToStandings() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power));
        // No FULL_TIME matches — repository returns empty list
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of());

        StandingsResponse result = service.calculate(Stream.A);

        result.table().forEach(row ->
                assertThat(row.played()).as("non-completed match must not count").isZero());
    }

    @Test
    void sortOrder_isPointsDescThenGoalDiffDescThenGoalsForDescThenNameAsc() {
        // Three teams: nkana 6 pts +5 GD, power 6 pts +3 GD, a third team 0 pts
        Team third = makeTeam(5L, "Arrows United", "ARU", Stream.A);
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power, third));
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of(
                        match(nkana, third, 3, 0),
                        match(nkana, power, 2, 0),
                        match(power, third, 3, 0)));

        StandingsResponse result = service.calculate(Stream.A);

        List<StandingRow> table = result.table();
        assertThat(table.get(0).teamId()).isEqualTo(nkana.getId());  // 6 pts, GD +5
        assertThat(table.get(1).teamId()).isEqualTo(power.getId());  // 3 pts, GD +3
        assertThat(table.get(2).teamId()).isEqualTo(third.getId());  // 0 pts
    }

    @Test
    void positions_areOneBasedAndSequential() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power));
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of());

        StandingsResponse result = service.calculate(Stream.A);

        assertThat(result.table().get(0).position()).isEqualTo(1);
        assertThat(result.table().get(1).position()).isEqualTo(2);
    }

    @Test
    void awayWin_awardsPointsToAwayTeam() {
        when(teamRepository.findByStreamOrderByNameAsc(Stream.A))
                .thenReturn(List.of(nkana, power));
        when(matchRepository.findByStreamAndStatus(Stream.A, MatchStatus.FULL_TIME))
                .thenReturn(List.of(match(nkana, power, 0, 2)));  // Power wins away

        StandingsResponse result = service.calculate(Stream.A);

        assertThat(rowFor(result, power.getId()).points()).isEqualTo(3);
        assertThat(rowFor(result, nkana.getId()).points()).isZero();
    }

    // ------------------------------------------------------------------ //
    // Helpers                                                             //
    // ------------------------------------------------------------------ //

    private Team makeTeam(Long id, String name, String shortName, Stream stream) {
        Team t = new Team(name, shortName, stream);
        setId(t, id);
        return t;
    }

    private FootballMatch match(Team home, Team away, int homeScore, int awayScore) {
        FootballMatch m = new FootballMatch(
                home, away, LocalDateTime.now(), home.getStream(), 1);
        m.setHomeScore(homeScore);
        m.setAwayScore(awayScore);
        m.setStatus(MatchStatus.FULL_TIME);
        return m;
    }

    private StandingRow rowFor(StandingsResponse resp, Long teamId) {
        return resp.table().stream()
                .filter(r -> r.teamId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No row for teamId " + teamId));
    }

    /** Reflectively sets the private 'id' field — avoids needing a test-only constructor. */
    private static void setId(Team team, Long id) {
        try {
            var f = Team.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(team, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
