package com.ai.mcp.weather.client.config;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.common.autoconfigure.McpClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 客户端配置 - HTTP Streamable 传输
 * <p>
 * Spring AI 2.0.0-M3 使用自动配置，无需手动创建 McpClient 和 Transport
 * 通过 application.yml 配置连接参数：
 * <pre>
 * spring:
 *   ai:
 *     mcp:
 *       client:
 *         enabled: true
 *         type: SYNC  # 或 ASYNC
 *         transport:
 *           type: HTTP
 *           url: http://localhost:8181
 * </pre>
 */
@Configuration
public class McpClientConfig {
    // Spring AI 2.0.0-M3 通过 McpClientAutoConfiguration 自动配置
    // SyncMcpToolCallbackProvider 由框架自动注入
}
