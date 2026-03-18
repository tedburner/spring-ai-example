package com.ai.mcp.weather.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiMcpWeatherClientApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAiMcpWeatherClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringAiMcpWeatherClientApplication.class, args);
        LOGGER.info("=================================================");
        LOGGER.info("MCP Weather Client 启动成功");
        LOGGER.info("API 端点:");
        LOGGER.info("  GET /weather-chat/query?query=xxx");
        LOGGER.info("  GET /weather-chat/stream-query?query=xxx");
        LOGGER.info("=================================================");
    }

}
