package com.zedpl.dto;

import com.zedpl.model.MatchEvent;
import com.zedpl.model.MatchEventType;

import java.time.LocalDateTime;

/**
 * Represents a single match event (goal, card, substitution, etc.).
 * Player is optional — some events may not be linked to a known player.
 */
public record MatchEventResponse(
        Long id,
        Long matchId,
        MatchEventType eventType,
        int minute,
        Integer additionalTime,
        TeamSummaryResponse team,
        PlayerSummaryResponse player,
        LocalDateTime createdAt
) {
    public static MatchEventResponse from(MatchEvent e) {
        return new MatchEventResponse(
                e.getId(),
                e.getMatch().getId(),
                e.getEventType(),
                e.getMinute(),
                e.getAdditionalTime(),
                TeamSummaryResponse.from(e.getTeam()),
                e.getPlayer() != null ? PlayerSummaryResponse.from(e.getPlayer()) : null,
                e.getCreatedAt()
        );
    }
}
