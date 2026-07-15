package com.janvi.urlshortener.url.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UrlAnalyticsResponse {

    private String shortCode;
    private String originalUrl;
    private long persistedClicks;
    private long pendingClicks;
    private long totalClicks;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}