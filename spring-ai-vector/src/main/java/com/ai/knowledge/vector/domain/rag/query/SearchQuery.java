package com.ai.knowledge.vector.domain.rag.query;

import lombok.Value;

/**
 * 查询实体 - 表示用户发起的搜索查询
 */
@Value
public class SearchQuery {
    String originalQuery;
    String rewrittenQuery;

    public SearchQuery(String originalQuery) {
        this.originalQuery = originalQuery;
        this.rewrittenQuery = originalQuery; // 初始化时与原查询相同
    }

    public SearchQuery(String originalQuery, String rewrittenQuery) {
        this.originalQuery = originalQuery;
        this.rewrittenQuery = rewrittenQuery != null ? rewrittenQuery : originalQuery;
    }

    /**
     * 创建改写后的查询
     */
    public SearchQuery withRewrite(String newRewrittenQuery) {
        return new SearchQuery(this.originalQuery, newRewrittenQuery);
    }
}