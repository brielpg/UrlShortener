package br.com.url_shortener.domain.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_urls")
public class Url {

    @Id
    @Column(name = "shorter_code", length = 5)
    private String shorterCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Url() {
    }

    public Url(String shorterCode, String originalUrl) {
        this.shorterCode = shorterCode;
        this.originalUrl = originalUrl;
        this.createdAt = LocalDateTime.now();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}
