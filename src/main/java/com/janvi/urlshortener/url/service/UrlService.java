package com.janvi.urlshortener.url.service;

import com.janvi.urlshortener.url.dto.CreateUrlRequest;
import com.janvi.urlshortener.url.dto.CreateUrlResponse;
import com.janvi.urlshortener.url.entity.Url;
import com.janvi.urlshortener.url.repository.UrlRepository;
import com.janvi.urlshortener.user.entity.User;
import com.janvi.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlService {

    private static final String BASE_URL = "http://localhost:8080/";

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    public CreateUrlResponse createShortUrl(CreateUrlRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        String shortCode = generateUniqueShortCode();

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
                .shortUrl(BASE_URL + savedUrl.getShortCode())
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

    public String getOriginalUrl(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        if (url.getExpiresAt() != null &&
                url.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Short URL has expired");
        }

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }
}