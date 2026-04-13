package com.ai.chat.application.service;

import com.ai.chat.domain.entity.SearchResult;
import com.ai.chat.domain.entity.TavilySearchResponse;
import com.ai.chat.interfaces.dto.AgentChatRequest;
import com.ai.chat.interfaces.dto.TavilySearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AgentService 单元测试
 *
 * @author: kiturone
 * @date: 2026/04/10
 */
class AgentServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ToolCallbackProvider toolCallbackProvider;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock ChatClient.Builder 构建 ChatClient
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        agentService = new AgentService(
                chatClientBuilder,
                List.of(toolCallbackProvider),
                "test-tavily-api-key"
        );
    }

    // ========== 构造函数测试 ==========

    @Test
    void testAgentServiceConstruction() {
        assertNotNull(agentService);
        verify(chatClientBuilder, times(1)).defaultSystem(anyString());
        verify(chatClientBuilder, times(1)).build();
    }

    @Test
    void testAgentServiceWithNullToolProviders() {
        AgentService serviceWithoutTools = new AgentService(
                chatClientBuilder,
                null,
                "test-key"
        );

        assertNotNull(serviceWithoutTools);
    }

    @Test
    void testAgentServiceWithEmptyToolProviders() {
        AgentService serviceWithEmptyTools = new AgentService(
                chatClientBuilder,
                List.of(),
                "test-key"
        );

        assertNotNull(serviceWithEmptyTools);
    }

    @Test
    void testAgentServiceWithNullApiKey() {
        AgentService serviceWithoutKey = new AgentService(
                chatClientBuilder,
                List.of(toolCallbackProvider),
                null
        );

        assertNotNull(serviceWithoutKey);
    }

    // ========== Tavily 搜索测试 ==========

    @Test
    void testTavilySearchWithEmptyApiKey() {
        // 创建一个没有 API Key 的 AgentService
        AgentService serviceWithoutKey = new AgentService(
                chatClientBuilder,
                List.of(toolCallbackProvider),
                ""  // 空 API Key
        );

        TavilySearchRequest request = TavilySearchRequest.builder()
                .query("test")
                .maxResults(5)
                .searchDepth("basic")
                .includeAnswer(true)
                .build();

        assertThrows(IllegalStateException.class, () -> {
            serviceWithoutKey.tavilySearch(request);
        });
    }

    @Test
    void testTavilySearchWithValidApiKeyButNetworkError() {
        TavilySearchRequest request = TavilySearchRequest.builder()
                .query("Spring AI")
                .maxResults(5)
                .searchDepth("basic")
                .includeAnswer(true)
                .build();

        // 由于 RestClient 会尝试真实连接，会抛出异常
        assertThrows(RuntimeException.class, () -> {
            agentService.tavilySearch(request);
        });
    }

    @Test
    void testSearchWebReturnsErrorOnFailure() {
        // searchWeb 内部捕获异常并返回 "搜索失败：..." 格式的字符串
        String result = agentService.searchWeb("test query");

        assertNotNull(result);
        assertTrue(result.contains("搜索失败"));
    }

    // ========== 请求 DTO 测试 ==========

    @Test
    void testAgentChatRequestBuilder() {
        AgentChatRequest request = AgentChatRequest.builder()
                .message("测试消息")
                .sessionId("session-123")
                .enableTools(true)
                .build();

        assertEquals("测试消息", request.getMessage());
        assertEquals("session-123", request.getSessionId());
        assertTrue(request.getEnableTools());
    }

    @Test
    void testAgentChatRequestDefaultValues() {
        AgentChatRequest request = AgentChatRequest.builder()
                .message("测试")
                .build();

        assertTrue(request.getEnableTools()); // 默认值为 true
    }

    @Test
    void testTavilySearchRequestBuilder() {
        TavilySearchRequest request = TavilySearchRequest.builder()
                .query("AI news")
                .maxResults(10)
                .searchDepth("advanced")
                .includeAnswer(false)
                .build();

        assertEquals("AI news", request.getQuery());
        assertEquals(10, request.getMaxResults());
        assertEquals("advanced", request.getSearchDepth());
        assertFalse(request.getIncludeAnswer());
    }

    @Test
    void testTavilySearchRequestDefaultValues() {
        TavilySearchRequest request = TavilySearchRequest.builder()
                .query("test")
                .build();

        assertEquals(5, request.getMaxResults()); // 默认值
        assertEquals("basic", request.getSearchDepth()); // 默认值
        assertTrue(request.getIncludeAnswer()); // 默认值
    }

    // ========== 实体类测试 ==========

    @Test
    void testSearchResultBuilder() {
        SearchResult result = SearchResult.builder()
                .url("https://example.com")
                .title("测试标题")
                .content("测试内容")
                .score(0.95)
                .build();

        assertEquals("https://example.com", result.getUrl());
        assertEquals("测试标题", result.getTitle());
        assertEquals("测试内容", result.getContent());
        assertEquals(0.95, result.getScore());
    }

    @Test
    void testTavilySearchResponseBuilder() {
        TavilySearchResponse response = TavilySearchResponse.builder()
                .query("test query")
                .answer("这是答案")
                .results(List.of(
                        SearchResult.builder()
                                .url("https://example.com")
                                .title("标题")
                                .content("内容")
                                .score(0.9)
                                .build()
                ))
                .images(List.of("image1.png", "image2.png"))
                .build();

        assertEquals("test query", response.getQuery());
        assertEquals("这是答案", response.getAnswer());
        assertNotNull(response.getResults());
        assertEquals(1, response.getResults().size());
        assertNotNull(response.getImages());
        assertEquals(2, response.getImages().size());
    }

    // ========== 边界条件测试 ==========

    @Test
    void testSearchResultWithNullValues() {
        SearchResult result = SearchResult.builder().build();

        assertNull(result.getUrl());
        assertNull(result.getTitle());
        assertNull(result.getContent());
        assertNull(result.getScore());
    }

    @Test
    void testTavilySearchResponseWithEmptyResults() {
        TavilySearchResponse response = TavilySearchResponse.builder()
                .query("test")
                .answer("答案")
                .results(List.of())
                .build();

        assertNotNull(response.getResults());
        assertEquals(0, response.getResults().size());
    }
}