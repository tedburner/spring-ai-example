package com.ai.chat.application.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * 日志拦截器服务
 *
 * @author kiturone
 * @date 2026/4/8
 */
@Service
public class LoggingInterceptorService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptorService.class);

    /**
     * 记录请求开始
     */
    public Instant logRequestStart(String sessionId, String userText) {
        Instant startTime = Instant.now();
        logger.info("[{}] 用户请求: {}", sessionId, userText);
        return startTime;
    }

    /**
     * 记录请求结束
     */
    public void logRequestEnd(String sessionId, Instant startTime) {
        long duration = Duration.between(startTime, Instant.now()).toMillis();
        logger.info("[{}] 请求完成，耗时: {}ms", sessionId, duration);
    }

    /**
     * 记录请求异常
     */
    public void logRequestError(String sessionId, Exception e) {
        logger.error("[{}] 请求异常: {}", sessionId, e.getMessage(), e);
    }
}