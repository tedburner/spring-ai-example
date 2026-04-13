package com.ai.chat.interfaces.controller;

import com.ai.chat.application.service.AgentService;
import com.ai.chat.domain.entity.SearchResult;
import com.ai.chat.domain.entity.TavilySearchResponse;
import com.ai.chat.interfaces.dto.AgentChatRequest;
import com.ai.chat.interfaces.dto.TavilySearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AgentController 单元测试
 *
 * @author: kiturone
 * @date: 2026/04/10
 */
class AgentControllerTest {

    @Mock
    private AgentService agentService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AgentController controller = new AgentController(agentService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    // ========== POST /agent/chat 测试 ==========

    @Test
    void testChatSuccess() {
        when(agentService.chat(any(AgentChatRequest.class)))
                .thenReturn("当前时间是 2026-04-10 12:00:00");

        webTestClient.post().uri("/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"现在几点了？\",\"sessionId\":\"session-123\",\"enableTools\":true}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(200)
                .jsonPath("$.data.message").isEqualTo("当前时间是 2026-04-10 12:00:00")
                .jsonPath("$.data.sessionId").isEqualTo("session-123");

        verify(agentService, times(1)).chat(any(AgentChatRequest.class));
    }

    @Test
    void testChatWithError() {
        when(agentService.chat(any(AgentChatRequest.class)))
                .thenThrow(new RuntimeException("LLM 服务异常"));

        webTestClient.post().uri("/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"测试\",\"sessionId\":\"s1\",\"enableTools\":true}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(-1)
                .jsonPath("$.data").isEqualTo("Agent 对话失败：LLM 服务异常");

        verify(agentService, times(1)).chat(any(AgentChatRequest.class));
    }

    // ========== POST /agent/stream 测试 ==========

    @Test
    void testStreamChatSuccess() {
        when(agentService.streamChat(any(AgentChatRequest.class)))
                .thenReturn(Flux.just("你好", "！"));

        webTestClient.post().uri("/agent/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"你好\",\"enableTools\":true}")
                .exchange()
                .expectStatus().isOk();

        verify(agentService, times(1)).streamChat(any(AgentChatRequest.class));
    }

    @Test
    void testStreamChatWithEmptyResponse() {
        when(agentService.streamChat(any(AgentChatRequest.class)))
                .thenReturn(Flux.empty());

        webTestClient.post().uri("/agent/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"测试\",\"enableTools\":false}")
                .exchange()
                .expectStatus().isOk();

        verify(agentService, times(1)).streamChat(any(AgentChatRequest.class));
    }

    // ========== POST /agent/chat/memory 测试 ==========

    @Test
    void testChatWithMemorySuccess() {
        when(agentService.chatWithMemory(any(AgentChatRequest.class), anyString()))
                .thenReturn("记住你之前说过的内容");

        webTestClient.post().uri("/agent/chat/memory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"我刚才说了什么？\",\"sessionId\":\"session-456\",\"enableTools\":true}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(200)
                .jsonPath("$.data.message").isEqualTo("记住你之前说过的内容")
                .jsonPath("$.data.sessionId").isEqualTo("session-456");

        verify(agentService, times(1)).chatWithMemory(any(AgentChatRequest.class), anyString());
    }

    @Test
    void testChatWithMemoryError() {
        when(agentService.chatWithMemory(any(AgentChatRequest.class), anyString()))
                .thenThrow(new IllegalStateException("会话过期"));

        webTestClient.post().uri("/agent/chat/memory")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"message\":\"测试\",\"sessionId\":\"expired\",\"enableTools\":true}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(-1)
                .jsonPath("$.data").isEqualTo("Agent 带记忆对话失败：会话过期");

        verify(agentService, times(1)).chatWithMemory(any(AgentChatRequest.class), anyString());
    }

    // ========== POST /agent/tavily/search 测试 ==========

    @Test
    void testTavilySearchPostSuccess() {
        TavilySearchResponse mockResponse = TavilySearchResponse.builder()
                .query("Spring AI")
                .answer("Spring AI 是一个用于构建 AI 应用的框架")
                .results(List.of(
                        SearchResult.builder()
                                .url("https://spring.io/ai")
                                .title("Spring AI 官网")
                                .content("Spring AI 官方文档")
                                .score(0.95)
                                .build()
                ))
                .build();

        when(agentService.tavilySearch(any(TavilySearchRequest.class)))
                .thenReturn(mockResponse);

        webTestClient.post().uri("/agent/tavily/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"query\":\"Spring AI\",\"maxResults\":5,\"searchDepth\":\"basic\",\"includeAnswer\":true}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(200)
                .jsonPath("$.data.query").isEqualTo("Spring AI")
                .jsonPath("$.data.answer").isEqualTo("Spring AI 是一个用于构建 AI 应用的框架")
                .jsonPath("$.data.results[0].title").isEqualTo("Spring AI 官网");

        verify(agentService, times(1)).tavilySearch(any(TavilySearchRequest.class));
    }

    @Test
    void testTavilySearchPostWithError() {
        when(agentService.tavilySearch(any(TavilySearchRequest.class)))
                .thenThrow(new IllegalStateException("Tavily API Key 未配置"));

        webTestClient.post().uri("/agent/tavily/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"query\":\"AI\",\"maxResults\":3}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(-1)
                .jsonPath("$.data").isEqualTo("Tavily 搜索失败：Tavily API Key 未配置");

        verify(agentService, times(1)).tavilySearch(any(TavilySearchRequest.class));
    }

    // ========== GET /agent/tavily/search 测试 ==========

    @Test
    void testTavilySearchGetSuccess() {
        TavilySearchResponse mockResponse = TavilySearchResponse.builder()
                .query("AI news")
                .answer("最新 AI 新闻")
                .results(List.of(
                        SearchResult.builder()
                                .url("https://news.example.com")
                                .title("AI 新闻")
                                .content("AI 最新进展")
                                .score(0.85)
                                .build()
                ))
                .build();

        when(agentService.tavilySearch(any(TavilySearchRequest.class)))
                .thenReturn(mockResponse);

        webTestClient.get().uri("/agent/tavily/search?query=AI news")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(200)
                .jsonPath("$.data.query").isEqualTo("AI news")
                .jsonPath("$.data.answer").isEqualTo("最新 AI 新闻");

        verify(agentService, times(1)).tavilySearch(any(TavilySearchRequest.class));
    }

    @Test
    void testTavilySearchGetError() {
        when(agentService.tavilySearch(any(TavilySearchRequest.class)))
                .thenThrow(new RuntimeException("网络连接失败"));

        webTestClient.get().uri("/agent/tavily/search?query=test")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(-1)
                .jsonPath("$.data").isEqualTo("Tavily 搜索失败：网络连接失败");

        verify(agentService, times(1)).tavilySearch(any(TavilySearchRequest.class));
    }
}
