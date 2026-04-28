package com.ai.chat.interfaces.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * @author: kiturone
 * @date: 2026/04/28
 * @description: Prompt Pattern 统一响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptPatternResponse {
    /**
     * Pattern 类型
     */
    private String type;

    /**
     * LLM 响应内容
     */
    private String result;

    /**
     * 使用的预设配置摘要
     */
    private String presetInfo;
}
