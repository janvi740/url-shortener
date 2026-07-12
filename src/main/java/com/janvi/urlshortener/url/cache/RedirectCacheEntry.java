package com.janvi.urlshortener.url.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedirectCacheEntry implements Serializable {

    private String originalUrl;
    private LocalDateTime expiresAt;
}