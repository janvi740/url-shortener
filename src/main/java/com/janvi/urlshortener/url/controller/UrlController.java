package com.janvi.urlshortener.url.controller;

import com.janvi.urlshortener.url.dto.CreateUrlRequest;
import com.janvi.urlshortener.url.dto.CreateUrlResponse;
import com.janvi.urlshortener.url.dto.UrlAnalyticsResponse;
import com.janvi.urlshortener.url.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse createShortUrl(
            @Valid @RequestBody CreateUrlRequest request
    ) {
        return urlService.createShortUrl(request);
    }

    @GetMapping("/{shortCode}/analytics")
    public UrlAnalyticsResponse getAnalytics(
            @PathVariable String shortCode
    ) {
        return urlService.getAnalytics(shortCode);
    }
}