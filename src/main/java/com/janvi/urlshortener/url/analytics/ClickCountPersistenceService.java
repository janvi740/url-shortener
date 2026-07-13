package com.janvi.urlshortener.url.analytics;

import com.janvi.urlshortener.url.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClickCountPersistenceService {

    private final UrlRepository urlRepository;

    @Transactional
    public void persistClicks(String shortCode, long delta) {

        if (delta <= 0) {
            return;
        }

        int updatedRows = urlRepository.incrementClickCount(
                shortCode,
                delta
        );

        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "URL not found for shortCode: " + shortCode
            );
        }
    }
}