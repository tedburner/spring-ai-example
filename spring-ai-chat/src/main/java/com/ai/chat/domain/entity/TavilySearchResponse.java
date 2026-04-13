package com.ai.chat.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Tavily 搜索响应实体
 *
 * @author kiturone
 * @date 2026/04/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TavilySearchResponse {

    /**
     * 原始查询
     */
    private String query;

    /**
     * 跟随答案
     */
    private String answer;

    /**
     * 搜索结果列表
     */
    private List<SearchResult> results;

    /**
     * 图像列表（可选）
     */
    private List<String> images;
}
