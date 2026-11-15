package br.com.url_shortener.infrastructure.controllers;

import br.com.url_shortener.application.dtos.UrlRequest;
import br.com.url_shortener.application.dtos.UrlResponse;
import br.com.url_shortener.application.services.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "URL Shortener", description = "Endpoints para encurtamento e redirecionamento de URLs")
public class UrlController {
    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    @Operation(summary = "Encurtar uma URL",
            description = "Recebe uma URL original e retorna um código curto para acessá-la.")
    @ApiResponse(responseCode = "201",
            description = "URL encurtada com sucesso",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UrlResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição")
    public ResponseEntity<UrlResponse> createShortCode(@Valid @RequestBody UrlRequest dto){
        String shortCode = service.createShortCode(dto.url());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UrlResponse(shortCode));
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirecionar para URL original",
            description = "Recebe um código curto e redireciona para a URL original.")
    @ApiResponse(responseCode = "308", description = "Redirecionamento permanente para a URL original")
    @ApiResponse(responseCode = "404", description = "Código curto não encontrado")
    public ResponseEntity<Void> getOriginalUrl(@PathVariable String shortCode) {
        String originalUrl = service.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
                .location(URI.create(originalUrl))
                .build();
    }
}
