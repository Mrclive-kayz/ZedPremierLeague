package com.zedpl.controller;

import com.zedpl.dto.*;
import com.zedpl.exception.GlobalExceptionHandler;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.exception.ResourceOwnershipException;
import com.zedpl.model.*;
import com.zedpl.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchController.class)
@Import(GlobalExceptionHandler.class)
class MatchControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean MatchService matchService;

    private static final TeamSummaryResponse HOME_TEAM = new TeamSummaryResponse(
            1L, "Nkana FC", "NKA", null, "Nkana Stadium", "Kitwe", Stream.A, 1935);
    private static final TeamSummaryResponse AWAY_TEAM = new TeamSummaryResponse(
            2L, "Power Dynamos", "PWD", null, "Dag Hammarskjold", "Kitwe", Stream.A, 1971);

    private static final MatchResponse MATCH = new MatchResponse(
            1L, HOME_TEAM, AWAY_TEAM,
            LocalDateTime.of(2026, 9, 14, 15, 0),
            "Nkana Stadium", Stream.A, 1, 2, 0, MatchStatus.FULL_TIME,
            null, null);

    // ------------------------------------------------------------------ //
    // General match list                                                  //
    // ------------------------------------------------------------------ //

    @Test
    void listMatches_returnsPagedResponse() throws Exception {
        var pagedResponse = new PagedResponse<>(List.of(MATCH), 0, 20, 1L, 1, true);
        when(matchService.findAll(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/matches").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].homeTeam.name", is("Nkana FC")))
                .andExpect(jsonPath("$.content[0].status", is("FULL_TIME")));
    }

    @Test
    void listMatches_withInvalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/matches").param("status", "UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void listMatches_withInvalidStream_returns400() throws Exception {
        mockMvc.perform(get("/api/matches").param("stream", "Z")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ //
    // Individual match                                                    //
    // ------------------------------------------------------------------ //

    @Test
    void getMatch_withValidId_returnsMatch() throws Exception {
        when(matchService.findById(1L)).thenReturn(MATCH);

        mockMvc.perform(get("/api/matches/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.matchWeek", is(1)));
    }

    @Test
    void getMatch_withUnknownId_returns404() throws Exception {
        when(matchService.findById(99L))
                .thenThrow(ResourceNotFoundException.forEntity("Match", 99L));

        mockMvc.perform(get("/api/matches/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ------------------------------------------------------------------ //
    // Upcoming / Recent                                                   //
    // ------------------------------------------------------------------ //

    @Test
    void upcomingMatches_returnsScheduledMatchesAscending() throws Exception {
        var pagedResponse = new PagedResponse<>(List.of(MATCH), 0, 10, 1L, 1, true);
        when(matchService.findUpcoming(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/matches/upcoming").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void recentMatches_returnsCompletedMatchesDescending() throws Exception {
        var pagedResponse = new PagedResponse<>(List.of(MATCH), 0, 10, 1L, 1, true);
        when(matchService.findRecent(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/matches/recent").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ------------------------------------------------------------------ //
    // Match events                                                        //
    // ------------------------------------------------------------------ //

    @Test
    void listMatchEvents_returnsEventList() throws Exception {
        var event = new MatchEventResponse(
                5L, 1L, MatchEventType.GOAL, 30, null,
                HOME_TEAM, null, LocalDateTime.now());
        when(matchService.findEventsByMatchId(1L)).thenReturn(List.of(event));

        mockMvc.perform(get("/api/matches/1/events").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType", is("GOAL")))
                .andExpect(jsonPath("$[0].minute", is(30)));
    }

    @Test
    void listMatchEvents_withUnknownMatchId_returns404() throws Exception {
        when(matchService.findEventsByMatchId(99L))
                .thenThrow(ResourceNotFoundException.forEntity("Match", 99L));

        mockMvc.perform(get("/api/matches/99/events").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMatchEvent_withOwnershipViolation_returns404() throws Exception {
        when(matchService.findEventByMatchAndEventId(1L, 99L))
                .thenThrow(new ResourceOwnershipException(
                        "Event 99 does not belong to match 1"));

        mockMvc.perform(get("/api/matches/1/events/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
