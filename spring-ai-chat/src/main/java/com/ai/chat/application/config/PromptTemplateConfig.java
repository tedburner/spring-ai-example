package com.ai.chat.application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: kiturone
 * @date: 2026/4/4
 * @description: 提示词模板配置类
 */
@Configuration
@EnableConfigurationProperties(PromptTemplateProperties.class)
public class PromptTemplateConfig {
    // 配置类启用 PromptTemplateProperties
}