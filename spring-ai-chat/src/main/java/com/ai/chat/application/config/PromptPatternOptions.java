package com.ai.chat.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: kiturone
 * @date: 2026/04/28
 * @description: Prompt Engineering Pattern 配置属性
 */
@ConfigurationProperties(prefix = "spring.ai.prompt-pattern")
public class PromptPatternOptions {

    private Map<String, PatternConfig> presets = new HashMap<>();

    /**
     * @param temperature 温度 (0.0-2.0)，越低输出越确定
     * @param topK 候选词采样上限，0 表示不限制
     * @param maxTokens 最大输出 token 数
     * @param votes 自一致性投票次数（仅 self-consistency 预设有效）
     */
    public record PatternConfig(double temperature, int topK, int maxTokens, int votes) {
        public PatternConfig(double temperature, int topK, int maxTokens) {
            this(temperature, topK, maxTokens, 5);
        }
    }

    public Map<String, PatternConfig> getPresets() {
        return presets;
    }

    public void setPresets(Map<String, PatternConfig> presets) {
        this.presets = presets;
    }

    /**
     * 获取指定 Pattern 的配置，不存在时返回 null
     */
    public PatternConfig get(String patternName) {
        return presets.get(patternName);
    }
}
