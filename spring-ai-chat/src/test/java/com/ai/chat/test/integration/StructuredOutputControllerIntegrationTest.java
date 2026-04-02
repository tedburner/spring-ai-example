package com.ai.chat.test.integration;

import com.ai.chat.test.config.TestControllerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(
    classes = {
        TestControllerConfig.class,
        com.ai.chat.interfaces.controller.ChatController.class,
        com.ai.chat.application.config.ChatMemoryConfig.class,
        com.ai.chat.application.service.StructuredOutputService.class,
        com.ai.chat.interfaces.controller.StructuredOutputController.class
    }
)
@AutoConfigureWebTestClient
class StructuredOutputControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testWeatherStructuredOutput() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("city", "Beijing");

        webTestClient.post()
                .uri("/structured/weather")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.city").isEqualTo("Beijing")
                .jsonPath("$.data.temperature").isEqualTo(25.5)
                .jsonPath("$.data.condition").isEqualTo("Sunny");
    }

    @Test
    void testTextAnalysisStructuredOutput() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", "This is a positive text for analysis");

        webTestClient.post()
                .uri("/structured/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.summary").isEqualTo("Test summary")
                .jsonPath("$.data.sentiment").isEqualTo("neutral")
                .jsonPath("$.data.confidence").isEqualTo(0.8);
    }

    @Test
    void testGenericStructuredOutput() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", "Beijing");
        requestBody.put("targetType", "weather");
        requestBody.put("prompt", "Analyze weather for Beijing");

        webTestClient.post()
                .uri("/structured/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data").exists();
    }

    @Test
    void testGenericStructuredOutputInvalidType() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", "test");
        requestBody.put("targetType", "invalid-type");

        webTestClient.post()
                .uri("/structured/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").exists();
    }
}