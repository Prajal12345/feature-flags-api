package com.featureflags.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FLAG_CACHE = "featureFlags";

    @Bean
    CacheManager cacheManager(
            @Value("${featureflags.cache.ttl-seconds:300}") long ttlSeconds,
            @Value("${featureflags.cache.max-size:1000}") long maxSize) {
        CaffeineCacheManager manager = new CaffeineCacheManager(FLAG_CACHE);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .maximumSize(maxSize)
                .recordStats());
        return manager;
    }
}
