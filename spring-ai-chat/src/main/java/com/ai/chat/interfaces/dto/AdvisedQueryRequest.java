package com.ai.chat.interfaces.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Advisor 增强查询请求
 *
 * @author kiturone
 * @date 2026/4/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvisedQueryRequest {
    /**
     * 用户查询
     */
    private String query;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 是否启用 Advisors
     */
    private boolean useAdvisors = true;

    /**
     * 选中的 Advisors（可选）
     */
    private List<String> selectedAdvisors;
}