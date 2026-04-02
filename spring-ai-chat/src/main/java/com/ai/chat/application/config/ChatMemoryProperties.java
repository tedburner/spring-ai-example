package com.ai.chat.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "spring.ai.chat.memory")
public class ChatMemoryProperties {

    /**
     * 是否启用聊天记忆功能
     */
    private boolean enabled = true;

    /**
     * 记忆存储类型: in_memory | redis
     */
    private String type = "in_memory";

    /**
     * 会话过期时间
     */
    private Duration ttl = Duration.ofMinutes(30);

    /**
     * 最大历史记录数
     */
    private int capacity = 10;

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}