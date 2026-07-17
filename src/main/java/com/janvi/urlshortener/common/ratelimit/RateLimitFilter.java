package com.janvi.urlshortener.common.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.janvi.urlshortener.common.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiter redisRateLimiter;
    private final ObjectMapper objectMapper;

    @Value("${app.rate-limit.create-url.max-requests}")
    private long maximumRequests;

    @Value("${app.rate-limit.create-url.window-seconds}")
    private long windowSeconds;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(
                "POST".equalsIgnoreCase(request.getMethod())
                        && "/api/urls".equals(request.getRequestURI())
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        /*
         * This filter runs after JWT authentication, so authenticated
         * requests should already contain the user's email.
         */
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = authentication.getName();

        String key = "rate:create-url:user:" + email;

        RateLimitDecision decision = redisRateLimiter.check(
                key,
                maximumRequests,
                Duration.ofSeconds(windowSeconds)
        );

        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(maximumRequests)
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(decision.remainingRequests())
        );

        if (!decision.allowed()) {
            writeRateLimitResponse(
                    request,
                    response,
                    decision.retryAfterSeconds()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeRateLimitResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            long retryAfterSeconds
    ) throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader(
                "Retry-After",
                String.valueOf(retryAfterSeconds)
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .message("URL creation rate limit exceeded")
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}