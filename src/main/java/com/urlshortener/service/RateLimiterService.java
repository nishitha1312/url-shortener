package com.urlshortener.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int SHORTEN_LIMIT_PER_MIN = 20;
    private static final int REDIRECT_LIMIT_PER_MIN = 100;

    private final Map<String, Bucket> shortenBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> redirectBuckets = new ConcurrentHashMap<>();

    public boolean allowShortenRequest(String clientIp) {
        Bucket bucket = shortenBuckets.computeIfAbsent(clientIp, this::newShortenBucket);
        return bucket.tryConsume(1);
    }

    public boolean allowRedirectRequest(String clientIp) {
        Bucket bucket = redirectBuckets.computeIfAbsent(clientIp, this::newRedirectBucket);
        return bucket.tryConsume(1);
    }

    private Bucket newShortenBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
            SHORTEN_LIMIT_PER_MIN,
            Refill.intervally(SHORTEN_LIMIT_PER_MIN, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newRedirectBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
            REDIRECT_LIMIT_PER_MIN,
            Refill.intervally(REDIRECT_LIMIT_PER_MIN, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }
}
