package com.janvi.urlshortener.url.service;
import org.springframework.beans.factory.annotation.Value;
import com.janvi.urlshortener.url.dto.CreateUrlRequest;
import com.janvi.urlshortener.url.dto.CreateUrlResponse;
import com.janvi.urlshortener.url.entity.Url;
import com.janvi.urlshortener.url.repository.UrlRepository;
import com.janvi.urlshortener.user.entity.User;
import com.janvi.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.janvi.urlshortener.common.exception.DuplicateResourceException;
import com.janvi.urlshortener.common.exception.InvalidAliasException;
import java.util.Locale;
import java.util.Set;
import com.janvi.urlshortener.url.cache.RedirectCacheEntry;
import com.janvi.urlshortener.url.cache.UrlCacheService;

import java.time.Duration;
import java.time.LocalDateTime;
import com.janvi.urlshortener.url.analytics.UrlClickCounterService;

@Service
@RequiredArgsConstructor
public class UrlService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlCacheService urlCacheService;
    private final UrlClickCounterService urlClickCounterService;

    private static final Set<String> RESERVED_ALIASES = Set.of(
            "api",
            "health",
            "login",
            "register",
            "admin",
            "error"
    );

    public CreateUrlResponse createShortUrl(CreateUrlRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        String shortCode = resolveShortCode(request.getCustomAlias());

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .user(user)
                .expiresAt(request.getExpiresAt())
                .build();

        Url savedUrl = urlRepository.save(url);

        return CreateUrlResponse.builder()
                .id(savedUrl.getId())
                .originalUrl(savedUrl.getOriginalUrl())
                .shortCode(savedUrl.getShortCode())
                .shortUrl(baseUrl + "/" + savedUrl.getShortCode())
                .expiresAt(savedUrl.getExpiresAt())
                .createdAt(savedUrl.getCreatedAt())
                .build();
    }

    private String generateUniqueShortCode() {
        String shortCode;

        do {
            shortCode = shortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(shortCode));

        return shortCode;
    }

    private String resolveShortCode(String customAlias) {

        if (customAlias == null || customAlias.isBlank()) {
            return generateUniqueShortCode();
        }

        String normalizedAlias = customAlias
                .trim()
                .toLowerCase(Locale.ROOT);

        if (RESERVED_ALIASES.contains(normalizedAlias)) {
            throw new InvalidAliasException(
                    "The alias '" + normalizedAlias + "' is reserved"
            );
        }

        if (urlRepository.existsByShortCode(normalizedAlias)) {
            throw new DuplicateResourceException(
                    "Custom alias is already in use"
            );
        }

        return normalizedAlias;
    }

    public String getOriginalUrl(String shortCode) {

        RedirectCacheEntry cachedEntry = urlCacheService.get(shortCode)
                .orElse(null);

        if (cachedEntry != null) {

            validateExpiry(cachedEntry.getExpiresAt());

            urlClickCounterService.increment(shortCode);

            return cachedEntry.getOriginalUrl();
        }

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new RuntimeException("Short URL not found")
                );

        validateExpiry(url.getExpiresAt());

        RedirectCacheEntry entry = new RedirectCacheEntry(
                url.getOriginalUrl(),
                url.getExpiresAt()
        );

        Duration ttl = calculateCacheTtl(url.getExpiresAt());

        urlCacheService.put(shortCode, entry, ttl);

        urlClickCounterService.increment(shortCode);

        return url.getOriginalUrl();
    }

    private void validateExpiry(LocalDateTime expiresAt) {

        if (expiresAt != null &&
                expiresAt.isBefore(LocalDateTime.now())) {

            throw new RuntimeException("Short URL has expired");
        }
    }

    private Duration calculateCacheTtl(LocalDateTime expiresAt) {

        Duration defaultTtl = Duration.ofHours(24);

        if (expiresAt == null) {
            return defaultTtl;
        }

        Duration remaining = Duration.between(
                LocalDateTime.now(),
                expiresAt
        );

        return remaining.compareTo(defaultTtl) < 0
                ? remaining
                : defaultTtl;
    }
}