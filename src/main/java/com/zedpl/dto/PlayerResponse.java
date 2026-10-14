package com.zedpl.dto;

import com.zedpl.model.PlayerPosition;
import com.zedpl.model.Player;

/**
 * Full player response including the team reference as a nested summary.
 */
public record PlayerResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        Integer shirtNumber,
        PlayerPosition position,
        String nationality,
        TeamSummaryResponse team,
        String createdAt,
        String updatedAt
) {
    public static PlayerResponse from(Player p) {
        return new PlayerResponse(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getFirstName() + " " + p.getLastName(),
                p.getShirtNumber(),
                p.getPosition(),
                p.getNationality(),
                TeamSummaryResponse.from(p.getTeam()),
                p.getCreatedAt() != null ? p.getCreatedAt().toString() : null,
                p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null
        );
    }
}
