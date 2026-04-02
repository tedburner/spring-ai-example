package com.ai.chat.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.ChatMemory;
import org.springframework.ai.chat.messages.InMemoryChatMemory;
import com.ai.chat.application.config.ChatMemoryProperties;
import com.ai.chat.application.metrics.MetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@TestConfiguration
public class TestControllerConfig {

    @Bean
    public ChatModel mockChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                String content = prompt.getInstructions().get(0).getContent().toLowerCase();

                if (content.contains("weather")) {
                    return new ChatResponse(new Generation("{\n" +
                            "  \"city\": \"Beijing\",\n" +
                            "  \"temperature\": 25.5,\n" +
                            "  \"condition\": \"Sunny\",\n" +
                            "  \"forecasts\": [\"Tomorrow Sunny\", \"Next Day Cloudy\"],\n" +
                            "  \"details\": {\n" +
                            "    \"humidity\": 60,\n" +
                            "    \"wind\": \"5km/h\"\n" +
                            "  }\n" +
                            "}"));
                } else if (content.contains("analyze") || content.contains("text")) {
                    return new ChatResponse(new Generation("{\n" +
                            "  \"summary\": \"Test summary\",\n" +
                            "  \"keywords\": [\"test\", \"keywords\"],\n" +
                            "  \"sentiment\": \"neutral\",\n" +
                            "  \"entities\": {\n" +
                            "    \"test\": 1,\n" +
                            "    \"keywords\": 1\n" +
                            "  },\n" +
                            "  \"confidence\": 0.8\n" +
                            "}"));
                } else {
                    return new ChatResponse(new Generation("Test response: " + content));
                }
            }
        };
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory(10, java.time.Duration.ofMinutes(30));
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public MetricsCollector metricsCollector(MeterRegistry meterRegistry) {
        return new MetricsCollector(meterRegistry);
    }

    @Bean
    public ChatMemoryProperties chatMemoryProperties() {
        ChatMemoryProperties properties = new ChatMemoryProperties();
        properties.setEnabled(true);
        properties.setType("in_memory");
        properties.setTtl(java.time.Duration.ofMinutes(30));
        properties.setCapacity(10);
        return properties;
    }
}