package br.com.url_shortener.application.services;

import br.com.url_shortener.domain.exceptions.UrlNotFoundException;
import br.com.url_shortener.domain.models.Url;
import br.com.url_shortener.infrastructure.repositories.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UrlServiceIntegrationTest {
    private static final String COUNTER_KEY = "url:counter";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private UrlRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UrlService urlService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp(){
        repository.deleteAll();

        Cache cache = cacheManager.getCache("url");
        if (cache != null)
            cache.clear();

        redisTemplate.delete(COUNTER_KEY);
    }

    @Test
    void initCounterShouldStartAfterMaxSequentialId() {
        repository.save(new Url("1jTi1", "https://www.test1.com", 100L));
        repository.save(new Url("2kYo2", "https://www.test2.com", 150L));
        repository.save(new Url("3lUp3", "https://www.test3.com", 120L));

        urlService.initCounter();
        assertEquals("150", redisTemplate.opsForValue().get(COUNTER_KEY));
    }

    @Test
    void initCounterShouldStartWithZero() {
        urlService.initCounter();
        assertEquals("0", redisTemplate.opsForValue().get(COUNTER_KEY));
    }

    @Test
    void initCounterShouldNotOverwriteExistingCounter(){
        redisTemplate.opsForValue().set(COUNTER_KEY, "200");
        repository.save(new Url("3kYp1", "https://www.test.com", 100L));

        urlService.initCounter();
        assertEquals("200", redisTemplate.opsForValue().get(COUNTER_KEY));
    }

    @Test
    void createShortCodeShouldCreateUrlInDatabase() {
        String originalUrl = "https://www.test.com";
        assertEquals(0, repository.findAll().size());

        String result = urlService.createShortCode(originalUrl);
        String url = urlService.getOriginalUrl(result);

        assertNotNull(result);
        assertEquals(1, repository.findAll().size());
        assertEquals(originalUrl, url);
    }

    @Test
    void createShortCodeShouldGenerateDifferentCodesForSameUrl() {
        String result1 = urlService.createShortCode("https://www.test.com");
        String result2 = urlService.createShortCode("https://www.test.com");

        assertNotEquals(result1, result2);
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void createShortCodeShouldIncrementCounter() {
        urlService.initCounter();
        assertEquals("0", redisTemplate.opsForValue().get(COUNTER_KEY));
        urlService.createShortCode("https://www.test1.com");
        assertEquals("1", redisTemplate.opsForValue().get(COUNTER_KEY));
        urlService.createShortCode("https://www.test2.com");
        assertEquals("2", redisTemplate.opsForValue().get(COUNTER_KEY));
    }

    @Test
    void getOriginalUrlShouldReturnOriginalUrlFromDatabaseAndCache() {
        String shortCode = "3kYp1";
        String originalUrl = "https://www.test.com";
        Url url = new Url(shortCode, originalUrl, 1L);
        repository.save(url);

        String databaseResult = urlService.getOriginalUrl(shortCode);
        assertEquals(originalUrl, databaseResult);
        repository.deleteById(shortCode);

        String cacheResult = urlService.getOriginalUrl(shortCode);
        assertEquals(originalUrl, cacheResult);
    }

    @Test
    void getOriginalUrlShouldThrowNotFoundExceptionUrl() {
        String shortCode = "3kYp1";

        assertThrows(UrlNotFoundException.class,
                () -> urlService.getOriginalUrl(shortCode));
    }
}