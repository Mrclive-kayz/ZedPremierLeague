package com.zedpl.dto;

import com.zedpl.model.Stream;
import com.zedpl.model.Team;

/**
 * Lean representation of a team used in list / standings responses.
 * Does not include the players collection to avoid N+1 queries.
 */
public record TeamSummaryResponse(
        Long id,
        String name,
        String shortName,
        String logoUrl,
        String stadium,
        String city,
        Stream stream,
        Integer foundedYear
) {
    public static TeamSummaryResponse from(Team t) {
        return new TeamSummaryResponse(
                t.getId(),
                t.getName(),
                t.getShortName(),
                t.getLogoUrl(),
                t.getStadium(),
                t.getCity(),
                t.getStream(),
                t.getFoundedYear()
        );
    }
}
