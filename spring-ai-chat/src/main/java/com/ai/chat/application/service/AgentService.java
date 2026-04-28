package com.ai.chat.application.service;

import com.ai.chat.domain.entity.TavilySearchResponse;
import com.ai.chat.interfaces.dto.AgentChatRequest;
import com.ai.chat.interfaces.dto.TavilySearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

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
    private final ChatClient advisedChatClient;
    private final Object[] toolCallbackProviders;
    private final TavilyService tavilyService;

    public AgentService(ChatClient.Builder builder,
                       List<ToolCallbackProvider> toolProviders,
                       TavilyService tavilyService,
                       List<Advisor> defaultAdvisors) {
        this.toolCallbackProviders = toolProviders.toArray();
        this.tavilyService = tavilyService;

        this.chatClient = builder
                .defaultSystem("你是一个智能助手，可以调用工具获取实时信息。当用户询问天气、时间、计算或需要搜索网络信息时，请使用相应的工具。")
                .build();

        this.advisedChatClient = builder
                .defaultSystem("你是一个智能助手，可以调用工具获取实时信息。当用户询问天气、时间、计算或需要搜索网络信息时，请使用相应的工具。")
                .defaultAdvisors(defaultAdvisors)
                .build();
    }

    /**
     * 非流式 Agent 对话
     */
    public String chat(AgentChatRequest request) {
        LOGGER.info("Agent 对话：{}", request.getMessage());

        return chatClient.prompt()
                .user(request.getMessage())
                .tools(toolCallbackProviders)
                .call()
                .content();
    }

    /**
     * 流式 Agent 对话（SSE）
     */
    public Flux<String> streamChat(AgentChatRequest request) {
        LOGGER.info("Agent 流式对话：{}", request.getMessage());

        return chatClient.prompt()
                .user(request.getMessage())
                .tools(toolCallbackProviders)
                .stream()
                .content();
    }

    /**
     * 带会话记忆的 Agent 对话
     * 通过 MessageChatMemoryAdvisor 自动管理对话历史
     */
    public String chatWithMemory(AgentChatRequest request) {
        String conversationId = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        LOGGER.info("Agent 带记忆对话：{}, sessionId={}", request.getMessage(), conversationId);

        return advisedChatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(request.getMessage())
                .tools(toolCallbackProviders)
                .call()
                .content();
    }

    /**
     * Tavily 网络搜索（委托给 TavilyService）
     */
    public TavilySearchResponse tavilySearch(TavilySearchRequest request) {
        return tavilyService.tavilySearch(request);
    }

    /**
     * 简化的 Tavily 搜索方法（委托给 TavilyService）
     */
    public String searchWeb(String query) {
        return tavilyService.searchWeb(query);
    }
}
