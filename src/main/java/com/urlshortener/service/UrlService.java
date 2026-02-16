package com.urlshortener.service;

import com.urlshortener.dto.UrlDtos;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.exception.ShortCodeConflictException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.Url;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.UrlEncoder;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UrlService {

    private static final Logger log = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UrlEncoder urlEncoder;
    private final MeterRegistry meterRegistry;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.default-expiry-days:30}")
    private int defaultExpiryDays;

    public UrlService(UrlRepository urlRepository,
                      ClickEventRepository clickEventRepository,
                      UrlEncoder urlEncoder,
                      MeterRegistry meterRegistry) {
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
        this.urlEncoder = urlEncoder;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public UrlDtos.ShortenResponse shortenUrl(UrlDtos.ShortenRequest request) {
        String shortCode = resolveShortCode(request);

        LocalDateTime expiresAt = request.getExpiresAt() != null
            ? request.getExpiresAt()
            : LocalDateTime.now().plusDays(defaultExpiryDays);

        Url url = Url.builder()
            .originalUrl(request.getOriginalUrl())
            .shortCode(shortCode)
            .customAlias(request.getCustomAlias())
            .expiresAt(expiresAt)
            .title(request.getTitle())
            .active(true)
            .build();

        urlRepository.save(url);
        meterRegistry.counter("urls.created").increment();
        log.info("Shortened URL: {} -> {}", request.getOriginalUrl(), shortCode);

        return buildShortenResponse(url);
    }

    private String resolveShortCode(UrlDtos.ShortenRequest request) {
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (urlRepository.existsByCustomAlias(request.getCustomAlias())) {
                throw new ShortCodeConflictException(
                    "Custom alias '" + request.getCustomAlias() + "' is already taken.");
            }
            return request.getCustomAlias();
        }
        String code;
        int attempts = 0;
        do {
            code = urlEncoder.generateShortCode();
            if (++attempts > 5) {
                throw new RuntimeException("Failed to generate unique short code after 5 attempts.");
            }
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    @Cacheable(value = "urls", key = "#shortCode", unless = "#result == null")
    public String getOriginalUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        if (!url.isActive() || url.isExpired()) {
            throw new UrlExpiredException(shortCode);
        }

        return url.getOriginalUrl();
    }

    @Transactional
    public void recordClick(String shortCode, String ip, String userAgent, String referer) {
        urlRepository.incrementClickCount(shortCode);

        ClickEvent event = ClickEvent.builder()
            .shortCode(shortCode)
            .ipAddress(ip)
            .userAgent(userAgent)
            .referer(referer)
            .build();

        clickEventRepository.save(event);
        meterRegistry.counter("urls.clicks", "shortCode", shortCode).increment();
    }

    public UrlDtos.UrlStatsResponse getUrlStats(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        return UrlDtos.UrlStatsResponse.builder()
            .shortCode(shortCode)
            .shortUrl(baseUrl + "/" + shortCode)
            .originalUrl(url.getOriginalUrl())
            .totalClicks(url.getClickCount())
            .createdAt(url.getCreatedAt())
            .expiresAt(url.getExpiresAt())
            .active(url.isActive() && !url.isExpired())
            .title(url.getTitle())
            .build();
    }

    public UrlDtos.AnalyticsResponse getAnalytics(String shortCode) {
        urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        long total = clickEventRepository.countByShortCode(shortCode);
        long last24h = clickEventRepository.countByShortCodeAndClickedAtAfter(
            shortCode, LocalDateTime.now().minusHours(24));
        long last7d = clickEventRepository.countByShortCodeAndClickedAtAfter(
            shortCode, LocalDateTime.now().minusDays(7));
        long last30d = clickEventRepository.countByShortCodeAndClickedAtAfter(
            shortCode, LocalDateTime.now().minusDays(30));

        List<Object[]> countryData = clickEventRepository.countByCountry(shortCode);
        Map<String, Long> byCountry = new HashMap<>();
        for (Object[] row : countryData) {
            String country = row[0] != null ? (String) row[0] : "Unknown";
            Long count = (Long) row[1];
            byCountry.put(country, count);
        }

        List<Object[]> dailyData = clickEventRepository.getDailyClickStats(
            shortCode, LocalDateTime.now().minusDays(30));
        List<UrlDtos.DailyClickStat> dailyStats = dailyData.stream()
            .map(row -> new UrlDtos.DailyClickStat(
                row[0].toString(),
                ((Number) row[1]).longValue()))
            .toList();

        return UrlDtos.AnalyticsResponse.builder()
            .shortCode(shortCode)
            .totalClicks(total)
            .clicksLast24Hours(last24h)
            .clicksLast7Days(last7d)
            .clicksLast30Days(last30d)
            .clicksByCountry(byCountry)
            .dailyStats(dailyStats)
            .build();
    }

    @Transactional
    @CacheEvict(value = "urls", key = "#shortCode")
    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        url.setActive(false);
        urlRepository.save(url);
        log.info("Deactivated short code: {}", shortCode);
    }

    @Scheduled(fixedRateString = "${app.cleanup-interval-ms:3600000}")
    @Transactional
    public void cleanupExpiredUrls() {
        int deactivated = urlRepository.deactivateExpiredUrls(LocalDateTime.now());
        if (deactivated > 0) {
            log.info("Deactivated {} expired URLs", deactivated);
            meterRegistry.counter("urls.expired").increment(deactivated);
        }
    }

    private UrlDtos.ShortenResponse buildShortenResponse(Url url) {
        return UrlDtos.ShortenResponse.builder()
            .shortCode(url.getShortCode())
            .shortUrl(baseUrl + "/" + url.getShortCode())
            .originalUrl(url.getOriginalUrl())
            .createdAt(url.getCreatedAt())
            .expiresAt(url.getExpiresAt())
            .title(url.getTitle())
            .build();
    }
}
