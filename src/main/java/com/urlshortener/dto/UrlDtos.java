package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UrlDtos {

    public static class ShortenRequest {
        @NotBlank(message = "URL must not be blank")
        @URL(message = "Must be a valid URL")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        private String originalUrl;

        @Pattern(regexp = "^[a-zA-Z0-9_-]{3,20}$",
                message = "Custom alias must be 3-20 alphanumeric characters, hyphens, or underscores")
        private String customAlias;
        private LocalDateTime expiresAt;
        private String title;

        public ShortenRequest() {}
        public String getOriginalUrl() { return originalUrl; }
        public void setOriginalUrl(String v) { this.originalUrl = v; }
        public String getCustomAlias() { return customAlias; }
        public void setCustomAlias(String v) { this.customAlias = v; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime v) { this.expiresAt = v; }
        public String getTitle() { return title; }
        public void setTitle(String v) { this.title = v; }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ShortenRequest r = new ShortenRequest();
            public Builder originalUrl(String v) { r.originalUrl = v; return this; }
            public Builder customAlias(String v) { r.customAlias = v; return this; }
            public Builder expiresAt(LocalDateTime v) { r.expiresAt = v; return this; }
            public Builder title(String v) { r.title = v; return this; }
            public ShortenRequest build() { return r; }
        }
    }

    public static class ShortenResponse {
        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private String title;
        public ShortenResponse() {}
        public String getShortCode() { return shortCode; }
        public void setShortCode(String v) { this.shortCode = v; }
        public String getShortUrl() { return shortUrl; }
        public void setShortUrl(String v) { this.shortUrl = v; }
        public String getOriginalUrl() { return originalUrl; }
        public void setOriginalUrl(String v) { this.originalUrl = v; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime v) { this.expiresAt = v; }
        public String getTitle() { return title; }
        public void setTitle(String v) { this.title = v; }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ShortenResponse r = new ShortenResponse();
            public Builder shortCode(String v) { r.shortCode = v; return this; }
            public Builder shortUrl(String v) { r.shortUrl = v; return this; }
            public Builder originalUrl(String v) { r.originalUrl = v; return this; }
            public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
            public Builder expiresAt(LocalDateTime v) { r.expiresAt = v; return this; }
            public Builder title(String v) { r.title = v; return this; }
            public ShortenResponse build() { return r; }
        }
    }

    public static class UrlStatsResponse {
        private String shortCode;
        private String shortUrl;
        private String originalUrl;
        private long totalClicks;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private boolean active;
        private String title;
        public UrlStatsResponse() {}
        public String getShortCode() { return shortCode; }
        public void setShortCode(String v) { this.shortCode = v; }
        public String getShortUrl() { return shortUrl; }
        public void setShortUrl(String v) { this.shortUrl = v; }
        public String getOriginalUrl() { return originalUrl; }
        public void setOriginalUrl(String v) { this.originalUrl = v; }
        public long getTotalClicks() { return totalClicks; }
        public void setTotalClicks(long v) { this.totalClicks = v; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime v) { this.expiresAt = v; }
        public boolean isActive() { return active; }
        public void setActive(boolean v) { this.active = v; }
        public String getTitle() { return title; }
        public void setTitle(String v) { this.title = v; }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final UrlStatsResponse r = new UrlStatsResponse();
            public Builder shortCode(String v) { r.shortCode = v; return this; }
            public Builder shortUrl(String v) { r.shortUrl = v; return this; }
            public Builder originalUrl(String v) { r.originalUrl = v; return this; }
            public Builder totalClicks(long v) { r.totalClicks = v; return this; }
            public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
            public Builder expiresAt(LocalDateTime v) { r.expiresAt = v; return this; }
            public Builder active(boolean v) { r.active = v; return this; }
            public Builder title(String v) { r.title = v; return this; }
            public UrlStatsResponse build() { return r; }
        }
    }

    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private long timestamp;
        public ErrorResponse() {}
        public int getStatus() { return status; }
        public void setStatus(int v) { this.status = v; }
        public String getError() { return error; }
        public void setError(String v) { this.error = v; }
        public String getMessage() { return message; }
        public void setMessage(String v) { this.message = v; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long v) { this.timestamp = v; }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final ErrorResponse r = new ErrorResponse();
            public Builder status(int v) { r.status = v; return this; }
            public Builder error(String v) { r.error = v; return this; }
            public Builder message(String v) { r.message = v; return this; }
            public Builder timestamp(long v) { r.timestamp = v; return this; }
            public ErrorResponse build() { return r; }
        }
    }

    public static class AnalyticsResponse {
        private String shortCode;
        private long totalClicks;
        private long clicksLast24Hours;
        private long clicksLast7Days;
        private long clicksLast30Days;
        private Map<String, Long> clicksByCountry;
        private List<DailyClickStat> dailyStats;
        public AnalyticsResponse() {}
        public String getShortCode() { return shortCode; }
        public void setShortCode(String v) { this.shortCode = v; }
        public long getTotalClicks() { return totalClicks; }
        public void setTotalClicks(long v) { this.totalClicks = v; }
        public long getClicksLast24Hours() { return clicksLast24Hours; }
        public void setClicksLast24Hours(long v) { this.clicksLast24Hours = v; }
        public long getClicksLast7Days() { return clicksLast7Days; }
        public void setClicksLast7Days(long v) { this.clicksLast7Days = v; }
        public long getClicksLast30Days() { return clicksLast30Days; }
        public void setClicksLast30Days(long v) { this.clicksLast30Days = v; }
        public Map<String, Long> getClicksByCountry() { return clicksByCountry; }
        public void setClicksByCountry(Map<String, Long> v) { this.clicksByCountry = v; }
        public List<DailyClickStat> getDailyStats() { return dailyStats; }
        public void setDailyStats(List<DailyClickStat> v) { this.dailyStats = v; }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final AnalyticsResponse r = new AnalyticsResponse();
            public Builder shortCode(String v) { r.shortCode = v; return this; }
            public Builder totalClicks(long v) { r.totalClicks = v; return this; }
            public Builder clicksLast24Hours(long v) { r.clicksLast24Hours = v; return this; }
            public Builder clicksLast7Days(long v) { r.clicksLast7Days = v; return this; }
            public Builder clicksLast30Days(long v) { r.clicksLast30Days = v; return this; }
            public Builder clicksByCountry(Map<String, Long> v) { r.clicksByCountry = v; return this; }
            public Builder dailyStats(List<DailyClickStat> v) { r.dailyStats = v; return this; }
            public AnalyticsResponse build() { return r; }
        }
    }

    public static class DailyClickStat {
        private String date;
        private long clicks;
        public DailyClickStat() {}
        public DailyClickStat(String date, long clicks) { this.date = date; this.clicks = clicks; }
        public String getDate() { return date; }
        public void setDate(String v) { this.date = v; }
        public long getClicks() { return clicks; }
        public void setClicks(long v) { this.clicks = v; }
    }
}
