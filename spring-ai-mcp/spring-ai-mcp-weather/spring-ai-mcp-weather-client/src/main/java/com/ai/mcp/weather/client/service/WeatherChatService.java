package com.ai.mcp.weather.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 天气聊天服务 - 整合 MCP 工具与 Ollama 模型
 */
@Service
public class WeatherChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherChatService.class);

    private final OllamaChatModel ollamaChatModel;
    private final SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    public WeatherChatService(OllamaChatModel ollamaChatModel,
                              SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        this.ollamaChatModel = ollamaChatModel;
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
    }

    /**
     * 非流式聊天 - 支持天气查询
     */
    public String chat(String query) {
        LOGGER.info("用户查询：{}", query);

        try {
            // 获取 MCP 工具回调
            var toolCallbacks = mcpToolCallbackProvider.getToolCallbacks();

            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model("deepseek-r1:1.5b")
                    .temperature(0.5)
                    .toolCallbacks(toolCallbacks)
                    .build();

            ChatResponse response = ollamaChatModel.call(
                    new Prompt(query, options)
            );

            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            LOGGER.error("聊天请求失败：{}", e.getMessage());
            return "抱歉，处理您的请求时出错：" + e.getMessage();
        }
    }

    /**
     * 流式聊天 - 支持天气查询
     */
    public Flux<String> streamChat(String query) {
        LOGGER.info("用户流式查询：{}", query);

        // 获取 MCP 工具回调
        var toolCallbacks = mcpToolCallbackProvider.getToolCallbacks();

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("deepseek-r1:1.5b")
                .temperature(0.5)
                .toolCallbacks(toolCallbacks)
                .build();

        return ollamaChatModel.stream(new Prompt(query, options))
                .map(response -> {
                    if (response.getResult() != null) {
                        response.getResult();
                        return response.getResult().getOutput().getText();
                    }
                    return "";
                })
                .filter(text -> !text.isEmpty());
    }
}
