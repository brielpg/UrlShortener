package br.com.url_shortener.domain.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_urls", indexes = {
        @Index(name = "idx_tb_urls_sequential_id", columnList = "sequential_id")
})
public class Url {

    @Id
    @Column(name = "shorter_code", length = 5)
    private String shorterCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sequential_id", nullable = false)
    private Long sequentialId;

    public Url() {
    }

    public Url(String shorterCode, String originalUrl, Long sequentialId) {
        this.shorterCode = shorterCode;
        this.originalUrl = originalUrl;
        this.sequentialId = sequentialId;
        this.createdAt = LocalDateTime.now();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}
