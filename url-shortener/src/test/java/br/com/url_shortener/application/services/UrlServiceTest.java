package br.com.url_shortener.application.services;

import br.com.url_shortener.domain.exceptions.ShortCodeRequiredException;
import br.com.url_shortener.domain.exceptions.UrlNotFoundException;
import br.com.url_shortener.domain.exceptions.UrlRequiredException;
import br.com.url_shortener.domain.models.Url;
import br.com.url_shortener.infrastructure.repositories.UrlRepository;
import org.hashids.Hashids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {
    private static final String COUNTER_KEY = "url:counter";

    @Mock
    private UrlRepository repository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Hashids hashids;

    @InjectMocks
    private UrlService urlService;

    @Test
    void createShortCodeShouldReturnSuccess() {
        String originalUrl = "https://www.exemplo.com";
        Long redisReturnId = 14776336L;
        String expectedShortCode = "3kYp1";

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(COUNTER_KEY))
                .thenReturn(redisReturnId);

        when(hashids.encode(redisReturnId))
                .thenReturn(expectedShortCode);

        String result = urlService.createShortCode(originalUrl);

        assertEquals(expectedShortCode, result);

        verify(repository).save(any(Url.class));
        verify(valueOperations).increment(COUNTER_KEY);
        verify(hashids).encode(redisReturnId);
    }

    @Test
    void createShortCodeShouldThrowExceptionNullUrl() {
        String originalUrl = null;

        assertThrows(UrlRequiredException.class, () ->
                urlService.createShortCode(originalUrl));

        verifyNoInteractions(redisTemplate, hashids, repository);
    }

    @Test
    void createShortCodeShouldThrowExceptionBlankUrl() {
        String originalUrl = "   ";

        assertThrows(UrlRequiredException.class, () ->
                urlService.createShortCode(originalUrl));

        verifyNoInteractions(redisTemplate, hashids, repository);
    }

    @Test
    void getOriginalUrlShouldReturnSuccess() {
        String shortCode = "3kYp1";
        Url url = new Url(shortCode, "https://www.exemplo.com", 14776336L);

        when(repository.findById(shortCode))
                .thenReturn(Optional.of(url));

        String result = urlService.getOriginalUrl(shortCode);

        assertEquals(url.getOriginalUrl(), result);

        verify(repository).findById(shortCode);
    }

    @Test
    void getOriginalUrlShouldThrowNotFoundExceptionUrl() {
        String shortCode = "3kYp1";

        when(repository.findById(shortCode))
                .thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () ->
                urlService.getOriginalUrl(shortCode));

        verify(repository).findById(shortCode);
    }

    @Test
    void getOriginalUrlShouldThrowExceptionNullShortCode() {
        String shortCode = null;

        assertThrows(ShortCodeRequiredException.class, () ->
                urlService.getOriginalUrl(shortCode));

        verifyNoInteractions(repository);
    }

    @Test
    void getOriginalUrlShouldThrowExceptionBlankShortCode() {
        String shortCode = "  ";

        assertThrows(ShortCodeRequiredException.class, () ->
                urlService.getOriginalUrl(shortCode));

        verifyNoInteractions(repository);
    }
}