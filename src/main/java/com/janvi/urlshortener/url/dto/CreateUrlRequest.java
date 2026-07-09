package com.janvi.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @org.hibernate.validator.constraints.URL(message = "Original URL must be valid")
    private String originalUrl;

    private LocalDateTime expiresAt;
}