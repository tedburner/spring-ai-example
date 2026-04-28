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
import org.springframework.ai.chat.client.advisor.api.Advisor;
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

    @Mock
    private TavilyService tavilyService;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock ChatClient.Builder 构建 ChatClient
        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.defaultAdvisors(anyList())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        agentService = new AgentService(
                chatClientBuilder,
                List.of(toolCallbackProvider),
                tavilyService,
                List.of()
        );
    }

    // ========== 构造函数测试 ==========

    @Test
    void testAgentServiceConstruction() {
        assertNotNull(agentService);
        verify(chatClientBuilder, atLeastOnce()).defaultSystem(anyString());
        verify(chatClientBuilder, atLeastOnce()).build();
    }

    @Test
    void testAgentServiceWithEmptyToolProviders() {
        AgentService serviceWithEmptyTools = new AgentService(
                chatClientBuilder,
                List.of(),
                tavilyService,
                List.of()
        );

        assertNotNull(serviceWithEmptyTools);
    }

    // ========== Tavily 搜索测试（委托给 TavilyService） ==========

    @Test
    void testTavilySearchDelegatesToTavilyService() {
        TavilySearchRequest request = TavilySearchRequest.builder()
                .query("Spring AI")
                .maxResults(5)
                .searchDepth("basic")
                .includeAnswer(true)
                .build();

        TavilySearchResponse mockResponse = TavilySearchResponse.builder()
                .query("Spring AI")
                .answer("Spring AI 是一个框架")
                .build();

        when(tavilyService.tavilySearch(request)).thenReturn(mockResponse);

        TavilySearchResponse result = agentService.tavilySearch(request);

        assertNotNull(result);
        assertEquals("Spring AI", result.getQuery());
        verify(tavilyService, times(1)).tavilySearch(request);
    }

    @Test
    void testSearchWebDelegatesToTavilyService() {
        String mockResult = "答案：Spring AI 是一个框架";
        when(tavilyService.searchWeb("Spring AI")).thenReturn(mockResult);

        String result = agentService.searchWeb("Spring AI");

        assertNotNull(result);
        assertEquals(mockResult, result);
        verify(tavilyService, times(1)).searchWeb("Spring AI");
    }

    @Test
    void testTavilySearchWithException() {
        TavilySearchRequest request = TavilySearchRequest.builder()
                .query("test")
                .build();

        when(tavilyService.tavilySearch(request))
                .thenThrow(new IllegalStateException("Tavily API Key 未配置"));

        assertThrows(IllegalStateException.class, () -> {
            agentService.tavilySearch(request);
        });
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