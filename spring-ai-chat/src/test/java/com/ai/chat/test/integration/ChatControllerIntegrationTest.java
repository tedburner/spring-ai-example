package com.ai.chat.test.integration;

import com.ai.chat.test.config.TestControllerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

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
class ChatControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testStreamQueryEndpoint() {
        webTestClient.get()
                .uri("/chat/stream-query?query=hello")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                .expectBody(String.class).consumeWith(response -> {
                    // 流式响应可能较难断言，但至少确保没有错误
                    System.out.println("Stream response: " + response.getResponseBody());
                });
    }

    @Test
    void testChatQueryEndpoint() {
        webTestClient.get()
                .uri("/chat/query?query=hello")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("test response"));
                });
    }

    @Test
    void testChatWithMemoryEndpoint() {
        String sessionId = "test-session";

        // 第一次请求
        webTestClient.get()
                .uri("/chat/memory/query?query=hello&sessionId=" + sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                });

        // 第二次请求，检查是否能记住上下文
        webTestClient.get()
                .uri("/chat/memory/query?query=what did we talk about&sessionId=" + sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                });
    }

    @Test
    void testChatWithMemoryStreamEndpoint() {
        String sessionId = "test-session-stream";

        webTestClient.get()
                .uri("/chat/memory/stream-query?query=hello&sessionId=" + sessionId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                .expectBody(String.class).consumeWith(response -> {
                    System.out.println("Memory stream response: " + response.getResponseBody());
                });
    }

    @Test
    void testActuatorEndpoints() {
        // 测试健康检查
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();

        // 测试指标端点
        webTestClient.get()
                .uri("/actuator/metrics")
                .exchange()
                .expectStatus().isOk();

        // 测试 Prometheus 端点
        webTestClient.get()
                .uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("text/plain");
    }
}