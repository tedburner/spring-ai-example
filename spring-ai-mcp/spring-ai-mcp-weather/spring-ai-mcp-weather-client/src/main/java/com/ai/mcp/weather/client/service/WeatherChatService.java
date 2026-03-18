package com.ai.mcp.weather.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 天气聊天服务 - 整合 MCP 工具与 Ollama 模型
 */
@Service
public class WeatherChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherChatService.class);

    private final OllamaChatModel ollamaChatModel;
    private final McpClient mcpClient;
    private List<ToolCallback> toolCallbacks;

    public WeatherChatService(OllamaChatModel ollamaChatModel, McpClient mcpClient) {
        this.ollamaChatModel = ollamaChatModel;
        this.mcpClient = mcpClient;
    }

    /**
     * 初始化时从 MCP Server 获取工具列表
     */
    @PostConstruct
    public void init() {
        try {
            // 列出 MCP Server 提供的所有工具
            toolCallbacks = mcpClient.listTools();
            LOGGER.info("成功加载 {} 个 MCP 工具", toolCallbacks.size());
            toolCallbacks.forEach(tool ->
                    LOGGER.info("工具: {} - {}", tool.getToolDefinition().name(),
                            tool.getToolDefinition().description()));
        } catch (Exception e) {
            LOGGER.error("初始化 MCP 工具失败: {}", e.getMessage());
        }
    }

    /**
     * 非流式聊天 - 支持天气查询
     */
    public String chat(String query) {
        LOGGER.info("用户查询: {}", query);

        try {
            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model("deepseek-r1:1.5b")
                    .temperature(0.5)
                    .tools(toolCallbacks)
                    .build();

            ChatResponse response = ollamaChatModel.call(
                    new Prompt(query, options)
            );

            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            LOGGER.error("聊天请求失败: {}", e.getMessage());
            return "抱歉，处理您的请求时出错: " + e.getMessage();
        }
    }

    /**
     * 流式聊天 - 支持天气查询
     */
    public Flux<String> streamChat(String query) {
        LOGGER.info("用户流式查询: {}", query);

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("deepseek-r1:1.5b")
                .temperature(0.5)
                .tools(toolCallbacks)
                .build();

        return ollamaChatModel.stream(new Prompt(query, options))
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        return response.getResult().getOutput().getText();
                    }
                    return "";
                })
                .filter(text -> !text.isEmpty());
    }
}
