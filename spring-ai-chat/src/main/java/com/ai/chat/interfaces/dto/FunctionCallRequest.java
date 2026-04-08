package com.ai.chat.interfaces.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 函数调用请求
 *
 * @author kiturone
 * @date 2026/4/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionCallRequest {
    /**
     * 用户查询
     */
    private String query;

    /**
     * 允许使用的工具列表（可选）
     */
    private List<String> allowedTools;

    /**
     * 会话ID（可选）
     */
    private String sessionId;
}