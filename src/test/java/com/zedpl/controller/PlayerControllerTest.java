package com.zedpl.controller;

import com.zedpl.dto.PagedResponse;
import com.zedpl.dto.PlayerResponse;
import com.zedpl.dto.TeamSummaryResponse;
import com.zedpl.exception.GlobalExceptionHandler;
import com.zedpl.exception.ResourceNotFoundException;
import com.zedpl.model.PlayerPosition;
import com.zedpl.model.Stream;
import com.zedpl.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
@Import(GlobalExceptionHandler.class)
class PlayerControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean PlayerService playerService;

    private static final TeamSummaryResponse TEAM = new TeamSummaryResponse(
            1L, "Nkana FC", "NKA", null, "Nkana Stadium", "Kitwe", Stream.A, 1935);

    private static final PlayerResponse PLAYER = new PlayerResponse(
            10L, "Samuel", "Phiri", "Samuel Phiri",
            7, PlayerPosition.MIDFIELDER, "Zambian", TEAM, null, null);

    @Test
    void listPlayers_returnsPagedResponse() throws Exception {
        PagedResponse<PlayerResponse> response = new PagedResponse<>(
                List.of(PLAYER), 0, 20, 1L, 1, true);
        when(playerService.findAll(eq(null), eq(null), any())).thenReturn(response);

        mockMvc.perform(get("/api/players").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName", is("Samuel")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void listPlayers_withInvalidPosition_returns400() throws Exception {
        mockMvc.perform(get("/api/players").param("position", "STRIKER")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void getPlayer_withValidId_returnsPlayer() throws Exception {
        when(playerService.findById(10L)).thenReturn(PLAYER);

        mockMvc.perform(get("/api/players/10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.position", is("MIDFIELDER")));
    }

    @Test
    void getPlayer_withUnknownId_returns404() throws Exception {
        when(playerService.findById(999L))
                .thenThrow(ResourceNotFoundException.forEntity("Player", 999L));

        mockMvc.perform(get("/api/players/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
