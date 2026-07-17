package com.janvi.urlshortener.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;

    /*
     * Performs INCR and sets expiry atomically.
     *
     * If the counter is created for the first time,
     * its TTL is also configured.
     */
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local current = redis.call('INCR', KEYS[1])

                    if current == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end

                    return current
                    """,
                    Long.class
            );

    public RateLimitDecision check(
            String key,
            long maximumRequests,
            Duration window
    ) {
        Long currentCount = redisTemplate.execute(
                INCREMENT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(window.toSeconds())
        );

        if (currentCount == null) {
            throw new IllegalStateException(
                    "Redis did not return a rate-limit counter"
            );
        }

        long remaining = Math.max(
                0,
                maximumRequests - currentCount
        );

        if (currentCount <= maximumRequests) {
            return new RateLimitDecision(
                    true,
                    remaining,
                    0
            );
        }

        Long ttl = redisTemplate.getExpire(
                key,
                java.util.concurrent.TimeUnit.SECONDS
        );

        long retryAfter = ttl == null || ttl < 0
                ? window.toSeconds()
                : ttl;

        return new RateLimitDecision(
                false,
                0,
                retryAfter
        );
    }
}