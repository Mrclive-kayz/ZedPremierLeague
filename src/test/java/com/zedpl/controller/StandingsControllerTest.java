package com.zedpl.controller;

import com.zedpl.dto.StandingRow;
import com.zedpl.dto.StandingsResponse;
import com.zedpl.exception.GlobalExceptionHandler;
import com.zedpl.model.Stream;
import com.zedpl.service.StandingsService;
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

@WebMvcTest(StandingsController.class)
@Import(GlobalExceptionHandler.class)
class StandingsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean StandingsService standingsService;

    @Test
    void getStandings_withStreamA_returnsTable() throws Exception {
        var row = new StandingRow(1, 1L, "Nkana FC", "NKA", null, Stream.A,
                2, 2, 0, 0, 6, 1, 5, 6);
        var response = new StandingsResponse(Stream.A, List.of(row));
        when(standingsService.calculate(Stream.A)).thenReturn(response);

        mockMvc.perform(get("/api/standings").param("stream", "A")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stream", is("A")))
                .andExpect(jsonPath("$.table", hasSize(1)))
                .andExpect(jsonPath("$.table[0].teamName", is("Nkana FC")))
                .andExpect(jsonPath("$.table[0].points", is(6)))
                .andExpect(jsonPath("$.table[0].goalDifference", is(5)))
                .andExpect(jsonPath("$.table[0].position", is(1)));
    }

    @Test
    void getStandings_missingStreamParam_returns400() throws Exception {
        mockMvc.perform(get("/api/standings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStandings_withInvalidStream_returns400() throws Exception {
        mockMvc.perform(get("/api/standings").param("stream", "C")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}
