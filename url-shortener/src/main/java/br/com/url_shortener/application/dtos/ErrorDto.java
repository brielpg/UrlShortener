package br.com.url_shortener.application.dtos;

public record ErrorDto(
        int value,
        String name,
        String message
) {
}
