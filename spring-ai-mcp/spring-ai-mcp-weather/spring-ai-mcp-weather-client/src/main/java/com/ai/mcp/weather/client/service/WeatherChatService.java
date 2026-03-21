package com.ai.mcp.weather.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 天气聊天服务 - 整合 MCP 工具与 Ollama 模型
 *
 * 模型配置从 application.yml 读取：
 * - spring.ai.ollama.chat.options.model
 * - spring.ai.ollama.chat.options.temperature
 */
@Service
public class WeatherChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherChatService.class);

    private final OllamaChatModel ollamaChatModel;
    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    public WeatherChatService(OllamaChatModel ollamaChatModel,
                              SyncMcpToolCallbackProvider toolCallbackProvider) {
        this.ollamaChatModel = ollamaChatModel;
        this.toolCallbackProvider = toolCallbackProvider;

        // 启动时记录工具加载情况
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        LOGGER.info("MCP 工具已加载：{} 个", callbacks.length);
        for (ToolCallback callback : callbacks) {
            LOGGER.info("  - 工具：{}", callback.getToolDefinition().name());
        }
    }

    /**
     * 非流式聊天 - 支持天气查询
     * 使用配置文件中的模型和温度设置
     */
    public String chat(String query) {
        LOGGER.info("用户查询：{}", query);

        try {
            var toolCallbacks = toolCallbackProvider.getToolCallbacks();

            // 使用配置文件的默认设置 + MCP 工具
            var options = OllamaChatOptions.builder()
                    .toolCallbacks(toolCallbacks)
                    .build();

            ChatResponse response = ollamaChatModel.call(
                    new Prompt(query, options)
            );

            String text = response.getResult().getOutput().getText();
            LOGGER.info("回复：{}", text);
            return text;
        } catch (Exception e) {
            LOGGER.error("聊天请求失败：{}", e.getMessage());
            return "抱歉，处理您的请求时出错：" + e.getMessage();
        }
    }

    /**
     * 流式聊天 - 支持天气查询
     * 使用配置文件中的模型和温度设置
     */
    public Flux<String> streamChat(String query) {
        LOGGER.info("用户流式查询：{}", query);

        var toolCallbacks = toolCallbackProvider.getToolCallbacks();

        // 使用配置文件的默认设置 + MCP 工具
        var options = OllamaChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .build();

        return ollamaChatModel.stream(new Prompt(query, options))
                .map(response -> {
                    if (response.getResult() != null) {
                        return response.getResult().getOutput().getText();
                    }
                    return "";
                })
                .filter(text -> !text.isEmpty());
    }
}