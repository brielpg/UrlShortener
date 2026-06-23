package br.com.url_shortener.application.services;

import br.com.url_shortener.domain.exceptions.UrlNotFoundException;
import br.com.url_shortener.domain.exceptions.UrlRequiredException;
import br.com.url_shortener.domain.models.Url;
import br.com.url_shortener.infrastructure.repositories.UrlRepository;
import jakarta.annotation.PostConstruct;
import org.hashids.Hashids;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {
    private static final String COUNTER_KEY = "url:counter";

    private final UrlRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final Hashids hashids;

    public UrlService(UrlRepository repository, StringRedisTemplate redisTemplate, Hashids hashids) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.hashids = hashids;
    }

    @PostConstruct
    public void initCounter() {
        Long maxId = repository.findMaxSequentialId();
        redisTemplate.opsForValue().setIfAbsent(COUNTER_KEY, String.valueOf(maxId));
    }

    @Transactional
    public String createShortCode(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank())
            throw new UrlRequiredException("URL is required");

        Long id = redisTemplate.opsForValue().increment(COUNTER_KEY);
        String shortCode = hashids.encode(id);

        Url shortUrl = new Url(shortCode, originalUrl, id);
        repository.save(shortUrl);

        return shortCode;
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(String shorterCode) {
        return repository.findById(shorterCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"))
                .getOriginalUrl();
    }
}
