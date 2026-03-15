package com.ai.mcp.weather.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 天气查询工具 - 使用 wttr.in 免费天气服务
 */
@Service
public class WeatherQueryTool {

    private static final String WTTR_IN_URL = "https://wttr.in/";

    private final WebClient webClient;

    public WeatherQueryTool(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 查询指定城市的天气
     *
     * @param city 城市名称（中文或英文）
     * @return 天气信息文本
     */
    @Tool(description = "查询指定城市的天气信息，返回温度、湿度、风速等")
    public String queryWeather(
            @ToolParam(description = "城市名称，如：北京、上海、Guangzhou") String city) {
        try {
            // 使用 wttr.in 的文本格式输出（添加 ?format=3 获取简洁格式）
            // format=3: 城市 + 温度 + 天气状况
            String url = WTTR_IN_URL + city + "?format=3&lang=zh";

            return webClient.get()
                    .uri(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "天气查询失败：" + e.getMessage();
        }
    }
}
