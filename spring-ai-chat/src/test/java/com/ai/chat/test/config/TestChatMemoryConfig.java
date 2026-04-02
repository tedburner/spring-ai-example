package com.ai.chat.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.chat.messages.ChatMemory;
import org.springframework.ai.chat.messages.InMemoryChatMemory;
import com.ai.chat.application.config.ChatMemoryProperties;

@TestConfiguration
public class TestChatMemoryConfig {

    @Bean
    public ChatMemoryProperties chatMemoryProperties() {
        ChatMemoryProperties properties = new ChatMemoryProperties();
        properties.setEnabled(true);
        properties.setType("in_memory");
        properties.setTtl(java.time.Duration.ofMinutes(30));
        properties.setCapacity(10);
        return properties;
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryProperties properties) {
        return new InMemoryChatMemory(properties.getCapacity(), properties.getTtl());
    }
}