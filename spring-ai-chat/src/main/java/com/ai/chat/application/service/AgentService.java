package com.ai.chat.application.service;

import com.ai.chat.domain.entity.SearchResult;
import com.ai.chat.domain.entity.TavilySearchResponse;
import com.ai.chat.interfaces.dto.AgentChatRequest;
import com.ai.chat.interfaces.dto.TavilySearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Agent 核心服务 - 支持工具调用和流式响应
 *
 * @author kiturone
 * @date 2026/04/10
 */
@Service
public class AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

    private final ChatClient chatClient;
    private final List<ToolCallbackProvider> toolProviders;
    private final RestClient restClient;

    @Value("${tavily.api.key:}")
    private String tavilyApiKey;

    public AgentService(ChatClient.Builder builder,
                       List<ToolCallbackProvider> toolProviders,
                       @Value("${tavily.api.key:}") String tavilyApiKey) {
        this.toolProviders = toolProviders;
        this.tavilyApiKey = tavilyApiKey;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.tavily.com")
                .defaultHeader("Content-Type", "application/json")
                .build();

        // 构建带工具的 ChatClient
        this.chatClient = builder
                .defaultSystem("你是一个智能助手，可以调用工具获取实时信息。当用户询问天气、时间、计算或需要搜索网络信息时，请使用相应的工具。")
                .build();
    }

    /**
     * 非流式 Agent 对话
     *
     * @param request 请求
     * @return 响应内容
     */
    public String chat(AgentChatRequest request) {
        LOGGER.info("Agent 对话：{}", request.getMessage());

        var spec = chatClient.prompt()
                .user(request.getMessage());

        // 启用工具调用
        if (request.getEnableTools() && !toolProviders.isEmpty()) {
            spec.tools(toolProviders);
        }

        return spec.call()
                .content();
    }

    /**
     * 流式 Agent 对话（SSE）
     *
     * @param request 请求
     * @return 响应流
     */
    public Flux<String> streamChat(AgentChatRequest request) {
        LOGGER.info("Agent 流式对话：{}", request.getMessage());

        var spec = chatClient.prompt()
                .user(request.getMessage());

        // 启用工具调用
        if (request.getEnableTools() && !toolProviders.isEmpty()) {
            spec.tools(toolProviders);
        }

        return spec.stream()
                .content();
    }

    /**
     * 带会话记忆的 Agent 对话
     *
     * @param request 请求
     * @param conversationId 会话 ID
     * @return 响应内容
     */
    public String chatWithMemory(AgentChatRequest request, String conversationId) {
        LOGGER.info("Agent 带记忆对话：{}, sessionId={}", request.getMessage(), conversationId);

        var spec = chatClient.prompt()
                .user(request.getMessage());

        // 启用工具调用
        if (request.getEnableTools() && !toolProviders.isEmpty()) {
            spec.tools(toolProviders);
        }

        // 使用 conversation id 保持上下文
        return spec.call()
                .content();
    }

    /**
     * Tavily 网络搜索
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    public TavilySearchResponse tavilySearch(TavilySearchRequest request) {
        LOGGER.info("Tavily 搜索：{}", request.getQuery());

        if (tavilyApiKey == null || tavilyApiKey.isEmpty()) {
            throw new IllegalStateException("Tavily API Key 未配置，请在 application.yml 中设置 tavily.api.key");
        }

        Map<String, Object> body = Map.of(
                "query", request.getQuery(),
                "api_key", tavilyApiKey,
                "max_results", request.getMaxResults(),
                "search_depth", request.getSearchDepth(),
                "include_answer", request.getIncludeAnswer()
        );

        try {
            ResponseEntity<TavilySearchResponse> response = restClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(TavilySearchResponse.class);

            return response.getBody();
        } catch (Exception e) {
            LOGGER.error("Tavily 搜索失败", e);
            throw new RuntimeException("Tavily 搜索失败：" + e.getMessage(), e);
        }
    }

    /**
     * 简化的 Tavily 搜索方法（供 Tool 调用）
     *
     * @param query 查询词
     * @return 搜索结果文本
     */
    public String searchWeb(String query) {
        try {
            TavilySearchRequest request = TavilySearchRequest.builder()
                    .query(query)
                    .maxResults(3)
                    .searchDepth("basic")
                    .includeAnswer(true)
                    .build();

            TavilySearchResponse response = tavilySearch(request);

            StringBuilder result = new StringBuilder();
            if (response.getAnswer() != null && !response.getAnswer().isEmpty()) {
                result.append("答案：").append(response.getAnswer()).append("\n\n");
            }

            if (response.getResults() != null && !response.getResults().isEmpty()) {
                result.append("相关链接:\n");
                for (int i = 0; i < Math.min(3, response.getResults().size()); i++) {
                    SearchResult r = response.getResults().get(i);
                    result.append(String.format("%d. [%s](%s)\n   %s\n",
                            i + 1, r.getTitle(), r.getUrl(), r.getContent()));
                }
            }

            return result.toString();
        } catch (Exception e) {
            return "搜索失败：" + e.getMessage();
        }
    }
}
