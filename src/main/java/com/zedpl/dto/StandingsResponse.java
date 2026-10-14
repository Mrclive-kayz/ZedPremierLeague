package com.zedpl.dto;

import com.zedpl.model.Stream;

import java.util.List;

/**
 * Full standings response for a stream, including the ordered table rows.
 */
public record StandingsResponse(
        Stream stream,
        List<StandingRow> table
) { }
