package com.dammroute.controller;

import com.dammroute.dto.RouteRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    private static final String API_KEY = "dammroute-hackathon-2026";

    @Test @DisplayName("GET /api/health returns 200 without auth")
    void health_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test @DisplayName("GET /api/clients returns 200 with valid API key")
    void getClients_validKey_returns200() throws Exception {
        mockMvc.perform(get("/api/clients").header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test @DisplayName("POST optimise with trusted carrier returns 201")
    void optimiseRoute_trustedCarrier_returns201() throws Exception {
        RouteRequestDTO request = new RouteRequestDTO(1L, List.of(1L, 2L, 3L));
        mockMvc.perform(post("/api/routes/optimise")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeId").exists())
                .andExpect(jsonPath("$.co2SavedPercent").isNumber());
    }

    @Test @DisplayName("GET /api/clients without API key returns 401")
    void getClients_noKey_returns401() throws Exception {
        mockMvc.perform(get("/api/clients")).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("POST optimise with BLOCKED carrier returns 403")
    void optimiseRoute_blockedCarrier_returns403() throws Exception {
        RouteRequestDTO request = new RouteRequestDTO(3L, List.of(1L, 2L, 3L));
        mockMvc.perform(post("/api/routes/optimise")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST optimise with empty body returns 400")
    void optimiseRoute_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/routes/optimise")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("GET /api/routes/999 returns 404")
    void getRoute_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/routes/999").header("X-API-Key", API_KEY))
                .andExpect(status().isNotFound());
    }
}
