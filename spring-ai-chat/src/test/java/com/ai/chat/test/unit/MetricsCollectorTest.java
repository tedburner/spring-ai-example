package com.ai.chat.test.unit;

import com.ai.chat.application.metrics.MetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    private MeterRegistry meterRegistry;
    private MetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsCollector = new MetricsCollector(meterRegistry);
    }

    @Test
    void testMetricsCollectorInitialization() {
        assertNotNull(metricsCollector);
        assertNotNull(meterRegistry);
    }

    @Test
    void testRecordChatDuration() {
        // 记录一些耗时数据
        metricsCollector.recordChatDuration("test-model", "test-operation", 1000L);
        metricsCollector.recordChatDuration("test-model", "test-operation", 2000L);

        // 验证计时器指标
        double totalTime = meterRegistry.get("spring.ai.chat.duration")
                .tag("model", "test-model")
                .tag("operation", "test-operation")
                .timer()
                .totalTime();

        assertTrue(totalTime >= 3000L);
    }

    @Test
    void testIncrementLlmCall() {
        // 增加调用次数
        metricsCollector.incrementLlmCall("test-model", "test-operation");
        metricsCollector.incrementLlmCall("test-model", "test-operation");
        metricsCollector.incrementLlmCall("another-model", "another-operation");

        // 验证计数器指标
        double callCount1 = meterRegistry.get("spring.ai.chat.calls")
                .tag("model", "test-model")
                .tag("operation", "test-operation")
                .counter()
                .count();

        double callCount2 = meterRegistry.get("spring.ai.chat.calls")
                .tag("model", "another-model")
                .tag("operation", "another-operation")
                .counter()
                .count();

        assertEquals(2.0, callCount1);
        assertEquals(1.0, callCount2);
    }

    @Test
    void testRecordTokenUsage() {
        // 记录 Token 使用量
        metricsCollector.recordTokenUsage(100L, "test-model");
        metricsCollector.recordTokenUsage(200L, "test-model");

        // 验证分布摘要指标
        double totalTokens = meterRegistry.get("spring.ai.tokens.used")
                .tag("model", "test-model")
                .summary()
                .totalAmount();

        assertEquals(300.0, totalTokens);
    }

    @Test
    void testSetActiveSessions() {
        // 设置活跃会话数
        metricsCollector.setActiveSessions(5);

        // 验证活跃会话数指标
        double activeSessions = meterRegistry.get("spring.ai.sessions.active")
                .gauge()
                .value();

        assertEquals(5.0, activeSessions);
    }
}