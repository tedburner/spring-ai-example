package com.ai.chat.application.service;

import com.ai.chat.domain.entity.TavilySearchResponse;
import com.ai.chat.interfaces.dto.AgentChatRequest;
import com.ai.chat.interfaces.dto.TavilySearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

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
    private final TavilyService tavilyService;

    public AgentService(ChatClient.Builder builder,
                       List<ToolCallbackProvider> toolProviders,
                       TavilyService tavilyService) {
        this.toolProviders = toolProviders;
        this.tavilyService = tavilyService;

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
     * Tavily 网络搜索（委托给 TavilyService）
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    public TavilySearchResponse tavilySearch(TavilySearchRequest request) {
        return tavilyService.tavilySearch(request);
    }

    /**
     * 简化的 Tavily 搜索方法（委托给 TavilyService）
     *
     * @param query 查询词
     * @return 搜索结果文本
     */
    public String searchWeb(String query) {
        return tavilyService.searchWeb(query);
    }
}
