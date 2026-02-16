package com.urlshortener.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * In-memory cache for local and test profiles (no Redis required).
 */
@Configuration
@Profile({"local", "test", "default"})
public class LocalCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("urls");
    }
}
