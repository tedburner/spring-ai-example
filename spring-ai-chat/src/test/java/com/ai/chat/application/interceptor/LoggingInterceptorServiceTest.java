package com.ai.chat.application.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: kiturone
 * @date: 2026/4/8
 * @description: LoggingInterceptorService 单元测试
 */
class LoggingInterceptorServiceTest {

    private LoggingInterceptorService loggingInterceptor;

    @BeforeEach
    void setUp() {
        loggingInterceptor = new LoggingInterceptorService();
    }

    @Test
    void testLogRequestStart() {
        String sessionId = "test-session";
        String userText = "测试问题";

        var startTime = loggingInterceptor.logRequestStart(sessionId, userText);

        assertNotNull(startTime);
    }

    @Test
    void testLogRequestEnd() {
        String sessionId = "test-session";
        var startTime = loggingInterceptor.logRequestStart(sessionId, "测试问题");

        assertDoesNotThrow(() -> {
            loggingInterceptor.logRequestEnd(sessionId, startTime);
        });
    }

    @Test
    void testLogRequestError() {
        String sessionId = "test-session";
        Exception exception = new RuntimeException("测试异常");

        assertDoesNotThrow(() -> {
            loggingInterceptor.logRequestError(sessionId, exception);
        });
    }

    @Test
    void testMultipleRequests() {
        String sessionId = "multi-session";

        var startTime1 = loggingInterceptor.logRequestStart(sessionId, "问题1");
        loggingInterceptor.logRequestEnd(sessionId, startTime1);

        var startTime2 = loggingInterceptor.logRequestStart(sessionId, "问题2");
        loggingInterceptor.logRequestEnd(sessionId, startTime2);

        assertNotNull(startTime1);
        assertNotNull(startTime2);
    }
}