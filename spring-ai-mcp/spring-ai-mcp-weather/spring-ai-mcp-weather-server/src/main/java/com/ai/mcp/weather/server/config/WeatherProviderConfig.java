package com.ai.mcp.weather.server.config;

import com.ai.mcp.weather.server.tool.WeatherQueryTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kiturone
 * @date 2026/3/21 19:53
 */
@Configuration
public class WeatherProviderConfig {

    /**
     * 注册天气查询工具
     */
    @Bean
    public ToolCallbackProvider weatherTools(WeatherQueryTool weatherQueryTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherQueryTool)
                .build();
    }
}
