package com.ai.chat.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 聊天请求 DTO
 *
 * @author kiturone
 * @date 2026/04/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatRequest {

    /**
     * 用户消息
     */
    private String message;

    /**
     * 会话 ID（可选，用于多轮对话）
     */
    private String sessionId;

    /**
     * 是否启用工具调用
     */
    @Builder.Default
    private Boolean enableTools = true;
}
