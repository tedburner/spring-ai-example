package com.ai.chat.application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class ChatMemoryConfig {
    // ChatMemory 将由 Spring AI 自动配置
    // 这里只用于启用配置属性
}