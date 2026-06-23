package br.com.url_shortener.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record UrlRequest(
        @NotBlank(message = "URL is required")
        String url
) {
}
