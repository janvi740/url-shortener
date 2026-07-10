package com.janvi.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateUrlRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "Original URL must be valid")
    private String originalUrl;

    @Size(
            min = 3,
            max = 30,
            message = "Custom alias must contain between 3 and 30 characters"
    )
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]*$",
            message = "Custom alias can contain only letters, numbers, hyphens and underscores"
    )
    private String customAlias;

    private LocalDateTime expiresAt;
}