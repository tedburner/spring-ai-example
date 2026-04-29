package com.ai.mcp.weather.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 集成测试 — 需要 MCP 天气服务器运行。
 */
@SpringBootTest
@Disabled("需要外部服务：MCP Weather Server (端口 8181) + Ollama (端口 11434)")
class SpringAiMcpWeatherClientApplicationTests {

    @Test
    void contextLoads() {
    }

}
