package com.ai.chat.application.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import java.util.Map;

/**
 * 日志记录 Advisor
 *
 * @author kiturone
 * @date 2026/04/28
 * @description 记录请求/响应日志和耗时
 */
public class LoggingAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);
    private static final String START_TIME_KEY = "__logging_start_time";

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        Map<String, Object> context = request.context();
        String sessionId = (String) context.getOrDefault("sessionId", "unknown");

        context.put(START_TIME_KEY, System.currentTimeMillis());

        String userText = request.prompt().getUserMessage().getText();
        String truncated = userText.length() > 100 ? userText.substring(0, 100) + "..." : userText;
        logger.info("[{}] 用户请求: {}", sessionId, truncated);

        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        Map<String, Object> context = response.context();
        String sessionId = (String) context.getOrDefault("sessionId", "unknown");
        long startTime = (long) context.getOrDefault(START_TIME_KEY, System.currentTimeMillis());
        long duration = System.currentTimeMillis() - startTime;
        logger.info("[{}] 请求完成，耗时: {}ms", sessionId, duration);
        return response;
    }
}
