package com.ai.chat.interfaces.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 模板查询请求
 *
 * @author kiturone
 * @date 2026/4/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateQueryRequest {
    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板变量
     */
    private Map<String, Object> variables;

    /**
     * 会话ID（可选）
     */
    private String sessionId;
}