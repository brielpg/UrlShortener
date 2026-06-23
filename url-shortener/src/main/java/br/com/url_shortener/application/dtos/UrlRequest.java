package br.com.url_shortener.application.dtos;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UrlRequest(
        @NotBlank(message = "URL is required")
        @URL(message = "Invalid URL format")
        String url
) {
}
