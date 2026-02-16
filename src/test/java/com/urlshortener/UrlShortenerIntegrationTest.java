package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.UrlDtos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/shorten → 201 with short URL")
    void shouldShortenUrl() throws Exception {
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("https://www.example.com/very/long/path")
            .build();

        MvcResult result = mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortCode", notNullValue()))
            .andExpect(jsonPath("$.shortUrl", containsString("localhost:8080")))
            .andExpect(jsonPath("$.originalUrl").value("https://www.example.com/very/long/path"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        UrlDtos.ShortenResponse response = objectMapper.readValue(body, UrlDtos.ShortenResponse.class);
        assertNotNull(response.getShortCode());
        assertTrue(response.getShortUrl().endsWith(response.getShortCode()));
    }

    @Test
    @DisplayName("POST /api/v1/shorten with custom alias → 201")
    void shouldShortenUrlWithCustomAlias() throws Exception {
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("https://github.com/spring-projects")
            .customAlias("github-sp")
            .build();

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortCode").value("github-sp"));
    }

    @Test
    @DisplayName("POST /api/v1/shorten duplicate alias → 409")
    void shouldReturnConflictForDuplicateAlias() throws Exception {
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("https://example.org")
            .customAlias("unique-alias-1")
            .build();

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Second request with same alias → conflict
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /{shortCode} → 302 redirect")
    void shouldRedirectToOriginalUrl() throws Exception {
        // Shorten first
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("https://www.google.com")
            .customAlias("google-test")
            .build();

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Now redirect
        mockMvc.perform(get("/google-test"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    @DisplayName("GET /{shortCode} unknown code → 404")
    void shouldReturn404ForUnknownShortCode() throws Exception {
        mockMvc.perform(get("/nonexistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/shorten with expired time → returns expired on redirect")
    void shouldReturn410ForExpiredUrl() throws Exception {
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("https://expired.example.com")
            .customAlias("expired-url")
            .expiresAt(LocalDateTime.now().minusMinutes(1)) // Already expired
            .build();

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/expired-url"))
            .andExpect(status().isGone());
    }

    @Test
    @DisplayName("GET /api/v1/urls/{shortCode}/stats → 200 with click count")
    void shouldReturnStats() throws Exception {
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("https://stats-test.example.com")
            .customAlias("stats-test")
            .build();

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/urls/stats-test/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value("stats-test"))
            .andExpect(jsonPath("$.totalClicks").value(0))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/urls/{shortCode} → 204, then redirect → 410")
    void shouldDeactivateUrl() throws Exception {
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("https://to-delete.example.com")
            .customAlias("delete-me")
            .build();

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/urls/delete-me"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/delete-me"))
            .andExpect(status().isGone());
    }

    @Test
    @DisplayName("POST /api/v1/shorten with blank URL → 400")
    void shouldReturn400ForBlankUrl() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/shorten with invalid URL → 400")
    void shouldReturn400ForInvalidUrl() throws Exception {
        UrlDtos.ShortenRequest request = UrlDtos.ShortenRequest.builder()
            .originalUrl("not-a-url")
            .build();

        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
