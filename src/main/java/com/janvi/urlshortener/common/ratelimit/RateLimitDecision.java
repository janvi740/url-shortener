package com.janvi.urlshortener.common.ratelimit;

public record RateLimitDecision(
        boolean allowed,
        long remainingRequests,
        long retryAfterSeconds
) {
}