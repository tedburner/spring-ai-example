package com.ai.chat.application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;

    // 预创建的指标实例，避免每次记录时重新创建
    private final Timer chatTimer;
    private final Counter llmCallCounter;
    private final AtomicInteger activeSessionsCounter;
    private final DistributionSummary tokenUsageSummary;

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 预创建基础指标
        this.chatTimer = Timer.builder("spring.ai.chat.duration")
                .description("LLM 调用耗时")
                .register(meterRegistry);

        this.llmCallCounter = Counter.builder("spring.ai.chat.calls")
                .description("LLM 调用次数")
                .register(meterRegistry);

        this.tokenUsageSummary = DistributionSummary.builder("spring.ai.tokens.used")
                .description("Token 使用量分布")
                .baseUnit("tokens")
                .register(meterRegistry);

        // 活跃会话数（使用 AtomicInteger 跟踪）
        this.activeSessionsCounter = new AtomicInteger(0);
        Gauge.builder("spring.ai.sessions.active", this.activeSessionsCounter, AtomicInteger::get)
                .description("活跃会话数")
                .register(meterRegistry);
    }

    public void recordChatDuration(String model, String operation, long durationMs) {
        // 使用预创建的计时器记录耗时
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(
            Timer.builder("spring.ai.chat.duration")
                .tag("model", model)
                .tag("operation", operation)
                .register(meterRegistry)
        );
    }

    public void incrementLlmCall(String model, String operation) {
        Counter.builder("spring.ai.chat.calls")
            .tag("model", model)
            .tag("operation", operation)
            .register(meterRegistry)
            .increment();
    }

    public void recordTokenUsage(long tokens, String model) {
        DistributionSummary.builder("spring.ai.tokens.used")
            .tag("model", model)
            .register(meterRegistry)
            .record(tokens);
    }

    public void setActiveSessions(int count) {
        activeSessionsCounter.set(count);
    }

    public int getActiveSessions() {
        return activeSessionsCounter.get();
    }

    // 为高频率操作提供更高效的方法
    public void recordOperation(String model, String operation, long durationMs, long tokens) {
        // 一次性记录多项指标
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(
            Timer.builder("spring.ai.chat.duration")
                .tag("model", model)
                .tag("operation", operation)
                .register(meterRegistry)
        );

        Counter.builder("spring.ai.chat.calls")
            .tag("model", model)
            .tag("operation", operation)
            .register(meterRegistry)
            .increment();

        if (tokens > 0) {
            DistributionSummary.builder("spring.ai.tokens.used")
                .tag("model", model)
                .register(meterRegistry)
                .record(tokens);
        }
    }
}