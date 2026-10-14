package com.zedpl.dto;

import com.zedpl.model.Stream;

/**
 * A single row in the league standings table.
 * Calculated from FULL_TIME matches — no persistence required.
 */
public record StandingRow(
        int position,
        Long teamId,
        String teamName,
        String teamShortName,
        String teamLogoUrl,
        Stream stream,
        int played,
        int won,
        int drawn,
        int lost,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        int points
) { }
