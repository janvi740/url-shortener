package com.janvi.urlshortener.url.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlCacheService {

    private static final String KEY_PREFIX = "url:redirect:";

    private final RedisTemplate<String, RedirectCacheEntry> redirectRedisTemplate;

    public Optional<RedirectCacheEntry> get(String shortCode) {
        RedirectCacheEntry entry = redirectRedisTemplate.opsForValue()
                .get(buildKey(shortCode));

        return Optional.ofNullable(entry);
    }

    public void put(
            String shortCode,
            RedirectCacheEntry entry,
            Duration ttl
    ) {
        redirectRedisTemplate.opsForValue()
                .set(buildKey(shortCode), entry, ttl);
    }

    public void delete(String shortCode) {
        redirectRedisTemplate.delete(buildKey(shortCode));
    }

    private String buildKey(String shortCode) {
        return KEY_PREFIX + shortCode;
    }
}