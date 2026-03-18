package com.ai.mcp.weather.client.controller;

import com.ai.mcp.weather.client.service.WeatherChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 天气聊天控制器
 */
@RestController
@RequestMapping("/weather-chat")
public class WeatherChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherChatController.class);

    private final WeatherChatService weatherChatService;

    public WeatherChatController(WeatherChatService weatherChatService) {
        this.weatherChatService = weatherChatService;
    }

    /**
     * 非流式天气查询
     * 示例: GET /weather-chat/query?query=北京今天天气怎么样？
     */
    @GetMapping("/query")
    public String query(@RequestParam("query") String query) {
        return weatherChatService.chat(query);
    }

    /**
     * 流式天气查询
     * 示例: GET /weather-chat/stream-query?query=上海明天会下雨吗？
     */
    @GetMapping(value = "/stream-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQuery(@RequestParam("query") String query) {
        return weatherChatService.streamChat(query);
    }
}
