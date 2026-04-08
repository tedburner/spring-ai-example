package com.ai.chat.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: kiturone
 * @date: 2026/4/4
 * @description: 提示词模板配置属性
 */
@ConfigurationProperties(prefix = "spring.ai.prompt-template")
public class PromptTemplateProperties {

    /**
     * 预定义的提示词模板
     */
    private Map<String, String> templates = new HashMap<>();

    /**
     * 默认角色
     */
    private String defaultRole = "AI助手";

    // Getters and Setters
    public Map<String, String> getTemplates() {
        return templates;
    }

    public void setTemplates(Map<String, String> templates) {
        this.templates = templates;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }
}