package com.ai.chat.application.config;

import com.ai.chat.application.advisor.LoggingAdvisor;
import com.ai.chat.application.advisor.RateLimitAdvisor;
import com.ai.chat.application.advisor.ValidationAdvisor;
import com.ai.chat.application.interceptor.RateLimitingInterceptorService;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Advisor 统一配置
 *
 * @author kiturone
 * @date 2026/04/28
 * @description 管理所有 Advisor、ChatMemory 及 Advisor 链的注册
 */
@Configuration
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class AdvisorConfig {

    @Bean
    public ValidationAdvisor validationAdvisor() {
        return new ValidationAdvisor();
    }

    @Bean
    public LoggingAdvisor loggingAdvisor() {
        return new LoggingAdvisor();
    }

    @Bean
    public RateLimitAdvisor rateLimitAdvisor(RateLimitingInterceptorService rateLimiter) {
        return new RateLimitAdvisor(rateLimiter);
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryProperties properties) {
        return MessageWindowChatMemory.builder()
                .maxMessages(properties.getCapacity())
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    public List<Advisor> defaultAdvisors(
            RateLimitAdvisor rateLimitAdvisor,
            ValidationAdvisor validationAdvisor,
            LoggingAdvisor loggingAdvisor,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor) {
        return List.of(
                rateLimitAdvisor,
                validationAdvisor,
                loggingAdvisor,
                messageChatMemoryAdvisor
        );
    }
}
