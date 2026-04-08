package com.ai.chat.application.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求限流拦截器服务
 *
 * @author kiturone
 * @date 2026/4/8
 */
@Service
public class RateLimitingInterceptorService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingInterceptorService.class);

    private final Map<String, RequestStats> requestStatsMap = new ConcurrentHashMap<>();

    // 配置参数
    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final long TIME_WINDOW_MS = 60000; // 60秒

    /**
     * 检查请求频率
     *
     * @param sessionId 会话ID
     * @throws IllegalStateException 如果请求频率超限
     */
    public void checkRateLimit(String sessionId) {
        // 处理 null session ID
        if (sessionId == null) {
            sessionId = "anonymous";
        }

        long currentTime = System.currentTimeMillis();

        RequestStats stats = requestStatsMap.computeIfAbsent(sessionId, k -> new RequestStats());

        synchronized (stats) {
            // 清理过期的请求记录
            if (currentTime - stats.windowStart > TIME_WINDOW_MS) {
                stats.reset(currentTime);
            }

            // 检查请求频率
            if (stats.requestCount.get() >= MAX_REQUESTS_PER_MINUTE) {
                logger.warn("会话 {} 请求频率超限: {} 次/分钟", sessionId, stats.requestCount.get());
                throw new IllegalStateException(
                    String.format("请求频率过高，请稍后再试。当前限制: %d 次/分钟", MAX_REQUESTS_PER_MINUTE)
                );
            }

            // 增加请求计数
            stats.requestCount.incrementAndGet();
            logger.debug("会话 {} 当前分钟请求次数: {}", sessionId, stats.requestCount.get());
        }
    }

    /**
     * 清理过期的会话记录（可定期调用）
     */
    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        requestStatsMap.entrySet().removeIf(entry ->
            currentTime - entry.getValue().windowStart > TIME_WINDOW_MS * 2
        );
        logger.debug("清理过期会话，剩余: {}", requestStatsMap.size());
    }

    /**
     * 获取当前活跃会话数
     */
    public int getActiveSessionsCount() {
        return requestStatsMap.size();
    }

    private static class RequestStats {
        long windowStart;
        AtomicInteger requestCount;

        RequestStats() {
            this.windowStart = System.currentTimeMillis();
            this.requestCount = new AtomicInteger(0);
        }

        void reset(long currentTime) {
            this.windowStart = currentTime;
            this.requestCount.set(0);
        }
    }
}