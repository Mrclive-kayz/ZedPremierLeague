package com.zedpl.dto;

import com.zedpl.model.Player;
import com.zedpl.model.PlayerPosition;

/**
 * Lightweight player reference used inside event responses to avoid deep nesting.
 */
public record PlayerSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        Integer shirtNumber,
        PlayerPosition position
) {
    public static PlayerSummaryResponse from(Player p) {
        return new PlayerSummaryResponse(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getFirstName() + " " + p.getLastName(),
                p.getShirtNumber(),
                p.getPosition()
        );
    }
}
