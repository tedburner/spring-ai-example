package com.ai.mcp.weather.server;

import com.ai.mcp.weather.server.tool.WeatherQueryTool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;

/**
 * Spring AI MCP 天气查询服务器
 */
@SpringBootApplication
public class SpringAiMcpWeatherServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiMcpWeatherServerApplication.class, args);
    }

}
