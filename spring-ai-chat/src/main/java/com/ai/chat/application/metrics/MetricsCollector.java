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

@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Timer.Builder chatTimerBuilder;
    private final Counter.Builder llmCallCounterBuilder;
    private final AtomicInteger activeSessionsCounter;
    private final DistributionSummary.Builder tokenUsageSummaryBuilder;

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // LLM 调用耗时统计构建器
        this.chatTimerBuilder = Timer.builder("spring.ai.chat.duration")
                .description("LLM 调用耗时");

        // 调用次数统计构建器
        this.llmCallCounterBuilder = Counter.builder("spring.ai.chat.calls")
                .description("LLM 调用次数");

        // Token 使用量统计构建器
        this.tokenUsageSummaryBuilder = DistributionSummary.builder("spring.ai.tokens.used")
                .description("Token 使用量分布")
                .baseUnit("tokens");

        // 活跃会话数（使用 AtomicInteger 跟踪）
        this.activeSessionsCounter = new AtomicInteger(0);
        Gauge.builder("spring.ai.sessions.active", activeSessionsCounter, AtomicInteger::get)
                .description("活跃会话数")
                .register(meterRegistry);
    }

    public void recordChatDuration(String model, String operation, long durationMs) {
        chatTimerBuilder
                .tag("model", model)
                .tag("operation", operation)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void incrementLlmCall(String model, String operation) {
        llmCallCounterBuilder
                .tag("model", model)
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    public void recordTokenUsage(long tokens, String model) {
        tokenUsageSummaryBuilder
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
}