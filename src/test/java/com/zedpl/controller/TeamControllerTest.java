package com.zedpl.controller;

import com.zedpl.dto.TeamSummaryResponse;
import com.zedpl.exception.GlobalExceptionHandler;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.model.Stream;
import com.zedpl.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link TeamController}.
 *
 * Uses @WebMvcTest to load only the web layer; the service is mocked.
 * Verifies HTTP status codes, JSON structure, and error responses.
 */
@WebMvcTest(TeamController.class)
@Import(GlobalExceptionHandler.class)
class TeamControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean TeamService teamService;

    private static final TeamSummaryResponse NKANA = new TeamSummaryResponse(
            1L, "Nkana FC", "NKA", null, "Nkana Stadium", "Kitwe", Stream.A, 1935);
    private static final TeamSummaryResponse POWER = new TeamSummaryResponse(
            2L, "Power Dynamos", "PWD", null, "Dag Hammarskjold Stadium", "Kitwe", Stream.A, 1971);

    @Test
    void listTeams_withNoFilter_returnsAllTeams() throws Exception {
        when(teamService.findAll(null)).thenReturn(List.of(NKANA, POWER));

        mockMvc.perform(get("/api/teams").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Nkana FC")))
                .andExpect(jsonPath("$[1].name", is("Power Dynamos")));
    }

    @Test
    void listTeams_withStreamFilter_callsServiceWithStream() throws Exception {
        when(teamService.findAll(Stream.A)).thenReturn(List.of(NKANA, POWER));

        mockMvc.perform(get("/api/teams").param("stream", "A").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(teamService).findAll(Stream.A);
    }

    @Test
    void listTeams_withInvalidStream_returns400() throws Exception {
        mockMvc.perform(get("/api/teams").param("stream", "X").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void getTeam_withValidId_returnsTeam() throws Exception {
        when(teamService.findById(1L)).thenReturn(NKANA);

        mockMvc.perform(get("/api/teams/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Nkana FC")))
                .andExpect(jsonPath("$.stream", is("A")));
    }

    @Test
    void getTeam_withUnknownId_returns404() throws Exception {
        when(teamService.findById(99L))
                .thenThrow(ResourceNotFoundException.forEntity("Team", 99L));

        mockMvc.perform(get("/api/teams/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", containsString("Team not found")));
    }

    @Test
    void getTeamPlayers_withValidId_returnsPlayerList() throws Exception {
        when(teamService.findPlayersByTeamId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/teams/1/players").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getTeamPlayers_withUnknownTeamId_returns404() throws Exception {
        when(teamService.findPlayersByTeamId(99L))
                .thenThrow(ResourceNotFoundException.forEntity("Team", 99L));

        mockMvc.perform(get("/api/teams/99/players").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
