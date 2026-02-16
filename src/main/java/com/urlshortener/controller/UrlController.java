package com.urlshortener.controller;

import com.urlshortener.dto.UrlDtos;
import com.urlshortener.service.RateLimiterService;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Tag(name = "URL Shortener", description = "APIs for shortening, resolving, and managing URLs")
public class UrlController {

    private final UrlService urlService;
    private final RateLimiterService rateLimiterService;

    public UrlController(UrlService urlService, RateLimiterService rateLimiterService) {
        this.urlService = urlService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/api/v1/shorten")
    @Operation(summary = "Shorten a URL")
    public ResponseEntity<UrlDtos.ShortenResponse> shortenUrl(
        @Valid @RequestBody UrlDtos.ShortenRequest request,
        HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.allowShortenRequest(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        UrlDtos.ShortenResponse response = urlService.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL")
    public ResponseEntity<Void> redirect(
        @PathVariable String shortCode,
        HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiterService.allowRedirectRequest(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        String originalUrl = urlService.getOriginalUrl(shortCode);
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        String referer = httpRequest.getHeader(HttpHeaders.REFERER);
        urlService.recordClick(shortCode, clientIp, userAgent, referer);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(originalUrl))
            .build();
    }

    @GetMapping("/api/v1/urls/{shortCode}/stats")
    @Operation(summary = "Get URL statistics")
    public ResponseEntity<UrlDtos.UrlStatsResponse> getStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getUrlStats(shortCode));
    }

    @GetMapping("/api/v1/urls/{shortCode}/analytics")
    @Operation(summary = "Get detailed click analytics")
    public ResponseEntity<UrlDtos.AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getAnalytics(shortCode));
    }

    @DeleteMapping("/api/v1/urls/{shortCode}")
    @Operation(summary = "Deactivate a short URL")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        urlService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
