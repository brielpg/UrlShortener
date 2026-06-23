package br.com.url_shortener.infrastructure.controllers;

import br.com.url_shortener.application.dtos.UrlRequest;
import br.com.url_shortener.application.dtos.UrlResponse;
import br.com.url_shortener.application.services.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class UrlController {
    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> createShortenUrl(@Valid @RequestBody UrlRequest dto){
        String shortUrl = service.createShortUrl(dto.url());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UrlResponse(shortUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> getOriginalUrl(@PathVariable String shortCode) {
        String originalUrl = service.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .location(URI.create(originalUrl))
                .build();
    }
}
