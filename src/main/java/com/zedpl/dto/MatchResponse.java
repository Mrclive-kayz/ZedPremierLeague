package com.zedpl.dto;

import com.zedpl.model.FootballMatch;
import com.zedpl.model.MatchStatus;
import com.zedpl.model.Stream;

import java.time.LocalDateTime;

/**
 * Full match response. Uses nested TeamSummaryResponse to avoid circular
 * references and to avoid exposing the players collection.
 */
public record MatchResponse(
        Long id,
        TeamSummaryResponse homeTeam,
        TeamSummaryResponse awayTeam,
        LocalDateTime matchDate,
        String venue,
        Stream stream,
        int matchWeek,
        int homeScore,
        int awayScore,
        MatchStatus status,
        String createdAt,
        String updatedAt
) {
    public static MatchResponse from(FootballMatch m) {
        return new MatchResponse(
                m.getId(),
                TeamSummaryResponse.from(m.getHomeTeam()),
                TeamSummaryResponse.from(m.getAwayTeam()),
                m.getMatchDate(),
                m.getVenue(),
                m.getStream(),
                m.getMatchWeek(),
                m.getHomeScore(),
                m.getAwayScore(),
                m.getStatus(),
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : null,
                m.getUpdatedAt() != null ? m.getUpdatedAt().toString() : null
        );
    }
}
