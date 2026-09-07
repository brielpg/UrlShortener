package br.com.url_shortener.infrastructure.controllers;

import br.com.url_shortener.domain.models.Url;
import br.com.url_shortener.infrastructure.repositories.UrlRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UrlControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    @Test
    void createShortCodeShouldReturn201Created() throws Exception {
        String requestBody = """
                {
                    "url": "https://www.exemplo.com"
                }
                """;

        ResultActions result = mockMvc.perform(
                post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        result.andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shortCode").isNotEmpty());
    }

    @Test
    void getOriginalUrlShouldReturn308PermanentRedirect() throws Exception {
        String shortCode = "3kYp1";
        String originalUrl = "https://www.exemplo.com";
        Url url = new Url(shortCode, originalUrl, 14776336L);
        urlRepository.save(url);

        ResultActions result = mockMvc.perform(
                get("/api/{shortCode}", shortCode)
        );

        result.andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", originalUrl));
    }

    @Test
    void getOriginalUrlShouldReturn404NotFound() throws Exception {
        ResultActions result = mockMvc.perform(
                get("/api/{shortcode}", "not-found")
        );

        result.andExpect(status().isNotFound());
    }
}