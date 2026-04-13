package com.ai.chat.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tavily 搜索请求 DTO
 *
 * @author kiturone
 * @date 2026/04/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TavilySearchRequest {

    /**
     * 搜索查询词
     */
    private String query;

    /**
     * 最大结果数
     */
    @Builder.Default
    private Integer maxResults = 5;

    /**
     * 搜索深度：basic 或 advanced
     */
    @Builder.Default
    private String searchDepth = "basic";

    /**
     * 是否包含答案
     */
    @Builder.Default
    private Boolean includeAnswer = true;
}
