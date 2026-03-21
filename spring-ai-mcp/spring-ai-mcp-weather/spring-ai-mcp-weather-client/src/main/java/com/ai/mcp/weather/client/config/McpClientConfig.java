package com.ai.mcp.weather.client.config;

import org.springframework.context.annotation.Configuration;

/**
 * MCP 客户端配置
 *
 * Spring AI 1.1.3+ 使用自动配置，通过 application.yml 配置连接参数。
 *
 * 配置示例：
 * <pre>
 * spring:
 *   ai:
 *     mcp:
 *       client:
 *         enabled: true
 *         type: SYNC
 *         sse:
 *           connections:
 *             weather-server:
 *               url: http://localhost:8181
 *               sse-endpoint: /sse
 * </pre>
 *
 * ToolCallback[] Bean 由 Spring AI 自动注入，
 * 包含所有已注册的 MCP 工具回调。
 */
@Configuration
public class McpClientConfig {
    // Spring AI 1.1.3 通过自动配置注入 ToolCallback[]
    // 无需手动配置 McpClient 或 Transport
}
