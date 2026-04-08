package com.ai.chat.application.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: kiturone
 * @date: 2026/4/8
 * @description: RateLimitingInterceptorService 单元测试
 */
class RateLimitingInterceptorServiceTest {

    private RateLimitingInterceptorService rateLimitingInterceptor;

    @BeforeEach
    void setUp() {
        rateLimitingInterceptor = new RateLimitingInterceptorService();
    }

    @Test
    void testValidRequestUnderLimit() {
        String sessionId = "test-session";

        // 应该不抛出异常（请求次数在限制内）
        assertDoesNotThrow(() -> {
            rateLimitingInterceptor.checkRateLimit(sessionId);
        });
    }

    @Test
    void testRequestOverLimit() {
        String sessionId = "limited-session";

        // 连续发送 20 次请求（达到上限）
        for (int i = 0; i < 20; i++) {
            assertDoesNotThrow(() -> {
                rateLimitingInterceptor.checkRateLimit(sessionId);
            });
        }

        // 第 21 次请求应该被限流
        assertThrows(IllegalStateException.class, () -> {
            rateLimitingInterceptor.checkRateLimit(sessionId);
        });
    }

    @Test
    void testDifferentSessionsTrackedIndependently() {
        String session1 = "session-1";
        String session2 = "session-2";

        // Session 1 发送 20 次请求
        for (int i = 0; i < 20; i++) {
            assertDoesNotThrow(() -> {
                rateLimitingInterceptor.checkRateLimit(session1);
            });
        }

        // Session 2 应该不受 Session 1 影响
        assertDoesNotThrow(() -> {
            rateLimitingInterceptor.checkRateLimit(session2);
        });
    }

    @Test
    void testGetActiveSessionsCount() {
        // 初始状态应该为 0
        assertEquals(0, rateLimitingInterceptor.getActiveSessionsCount());

        // 创建几个会话
        rateLimitingInterceptor.checkRateLimit("session-1");
        rateLimitingInterceptor.checkRateLimit("session-2");

        // 应该有 2 个活跃会话
        assertEquals(2, rateLimitingInterceptor.getActiveSessionsCount());
    }

    @Test
    void testCleanupExpiredSessions() {
        // 创建会话
        rateLimitingInterceptor.checkRateLimit("test-session");

        assertEquals(1, rateLimitingInterceptor.getActiveSessionsCount());

        // 清理过期会话
        rateLimitingInterceptor.cleanupExpiredSessions();

        // 由于时间窗口未过期，会话应该仍然存在
        assertTrue(rateLimitingInterceptor.getActiveSessionsCount() >= 1);
    }

    @Test
    void testRequestLimitMessage() {
        String sessionId = "test-session";

        // 达到上限
        for (int i = 0; i < 20; i++) {
            rateLimitingInterceptor.checkRateLimit(sessionId);
        }

        // 超过上限时应该抛出包含限制信息的异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rateLimitingInterceptor.checkRateLimit(sessionId);
        });

        assertTrue(exception.getMessage().contains("20"));
        assertTrue(exception.getMessage().contains("次/分钟"));
    }

    @Test
    void testMultipleRequestsSingleSession() {
        String sessionId = "single-session";

        // 发送多次请求（未超限）
        for (int i = 0; i < 10; i++) {
            assertDoesNotThrow(() -> {
                rateLimitingInterceptor.checkRateLimit(sessionId);
            });
        }
    }

    @Test
    void testNullSessionId() {
        // null session ID 应该也能工作
        assertDoesNotThrow(() -> {
            rateLimitingInterceptor.checkRateLimit(null);
        });
    }
}