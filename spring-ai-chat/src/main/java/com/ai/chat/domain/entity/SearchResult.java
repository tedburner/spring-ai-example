package com.ai.chat.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tavily 搜索结果实体
 *
 * @author kiturone
 * @date 2026/04/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    /**
     * 搜索结果 URL
     */
    private String url;

    /**
     * 搜索结果内容
     */
    private String content;

    /**
     * 搜索结果标题
     */
    private String title;

    /**
     * 相关性分数
     */
    private Double score;
}
