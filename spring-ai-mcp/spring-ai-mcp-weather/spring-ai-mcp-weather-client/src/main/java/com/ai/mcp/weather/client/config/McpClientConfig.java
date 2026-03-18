package com.ai.mcp.weather.client.config;

import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.transport.http.HttpStreamableMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * MCP 客户端配置 - HTTP Streamable 传输
 */
@Configuration
public class McpClientConfig {

    @Value("${spring.ai.mcp.client.transport.url:http://localhost:8181}")
    private String mcpServerUrl;

    /**
     * 配置 MCP 客户端，使用 HTTP Streamable (SSE) 传输
     */
    @Bean
    public McpClient mcpClient(WebClient.Builder webClientBuilder) {
        // 创建 HTTP Streamable 传输层
        HttpStreamableMcpTransport transport = new HttpStreamableMcpTransport(
                mcpServerUrl,
                webClientBuilder.build()
        );

        // 创建并返回 MCP 客户端
        return McpClient.builder(transport)
                .clientInfo("weather-client", "1.0.0")
                .build();
    }
}
