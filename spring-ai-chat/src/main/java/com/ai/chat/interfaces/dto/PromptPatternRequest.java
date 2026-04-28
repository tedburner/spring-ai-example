package com.ai.chat.interfaces.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author: kiturone
 * @date: 2026/04/28
 * @description: Prompt Pattern 统一请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptPatternRequest {
    /**
     * Pattern 类型：zero-shot, few-shot, system, role, contextual, cot,
     *               code-write, code-explain, code-translate,
     *               step-back, self-consistency, tree-of-thoughts, auto-prompt
     */
    private String type;

    /**
     * 主提示词/问题
     */
    private String prompt;

    /**
     * System 消息（system/role pattern 使用）
     */
    private String system;

    /**
     * 角色描述（role pattern 使用）
     */
    private String role;

    /**
     * 模板变量（contextual pattern 使用）
     */
    private Map<String, Object> variables;

    /**
     * Few-shot 示例列表
     */
    private List<Example> examples;

    /**
     * 目标语言（code-translate 使用）
     */
    private String targetLanguage;

    /**
     * 投票次数/分支数（self-consistency / tree-of-thoughts 使用）
     */
    private Integer count;

    /**
     * 代码内容（code-explain / code-translate 使用）
     */
    private String code;

    /**
     * CoT 示例（chain-of-thought-few-shot 使用）
     */
    private String cotExample;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Example {
        private String input;
        private String output;
    }
}
