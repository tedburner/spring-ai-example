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
        com.ai.chat.SpringAiChatApplication.class // 使用主应用类
    }
)
@AutoConfigureWebTestClient
class FullIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testFullFeatureIntegration() {
        // 1. 测试基本聊天功能
        testBasicChat();

        // 2. 测试带记忆的聊天功能
        testChatWithMemory();

        // 3. 测试结构化输出功能
        testStructuredOutput();

        // 4. 测试监控功能
        testMonitoringEndpoints();
    }

    private void testBasicChat() {
        webTestClient.get()
                .uri("/chat/query?query=hello")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.toLowerCase().contains("test response"));
                });
    }

    private void testChatWithMemory() {
        String sessionId = "integration-test-session";

        // 发起两次请求，验证记忆功能
        webTestClient.get()
                .uri("/chat/memory/query?query=hello&sessionId=" + sessionId)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/chat/memory/query?query=what did I say&sessionId=" + sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                });
    }

    private void testStructuredOutput() {
        Map<String, String> weatherRequest = new HashMap<>();
        weatherRequest.put("city", "Shanghai");

        webTestClient.post()
                .uri("/structured/weather")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(weatherRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.city").isEqualTo("Beijing"); // Mock 返回固定值
    }

    private void testMonitoringEndpoints() {
        // 验证监控端点是否可用
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/actuator/metrics")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("text/plain");
    }

    private void assertNotNull(String body) {
        org.junit.jupiter.api.Assertions.assertNotNull(body);
    }

    private void assertTrue(boolean contains) {
        org.junit.jupiter.api.Assertions.assertTrue(contains);
    }
}