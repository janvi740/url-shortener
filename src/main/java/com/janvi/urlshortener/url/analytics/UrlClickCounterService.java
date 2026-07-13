package com.janvi.urlshortener.url.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlClickCounterService {

    private static final String KEY_PREFIX = "url:clicks:delta:";

    private final StringRedisTemplate stringRedisTemplate;

    public void increment(String shortCode) {
        try {
            stringRedisTemplate.opsForValue()
                    .increment(buildKey(shortCode));
        } catch (DataAccessException ex) {
            log.warn(
                    "Failed to increment click counter for shortCode={}",
                    shortCode,
                    ex
            );
        }
    }

    public Long getPendingClicks(String shortCode) {
        String value = stringRedisTemplate.opsForValue()
                .get(buildKey(shortCode));

        return value == null ? 0L : Long.parseLong(value);
    }

    public long drainPendingClicks(String shortCode) {
        String value = stringRedisTemplate.opsForValue()
                .getAndDelete(buildKey(shortCode));

        return value == null ? 0L : Long.parseLong(value);
    }

    public void restorePendingClicks(String shortCode, long delta) {
        if (delta > 0) {
            stringRedisTemplate.opsForValue()
                    .increment(buildKey(shortCode), delta);
        }
    }

    public Set<String> findShortCodesWithPendingClicks() {
        Set<String> shortCodes = new HashSet<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(KEY_PREFIX + "*")
                .count(100)
                .build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();

                shortCodes.add(
                        key.substring(KEY_PREFIX.length())
                );
            }
        }

        return shortCodes;
    }

    private String buildKey(String shortCode) {
        return KEY_PREFIX + shortCode;
    }
}