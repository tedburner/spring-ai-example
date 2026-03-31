package com.ai.knowledge.vector.domain.rag.query;

import lombok.Value;

import java.util.List;

/**
 * 查询改写结果 - 包含改写后的查询及分解结果
 */
@Value
public class QueryRewriteResult {
    String originalQuery;
    String rewrittenQuery;
    List<String> decomposedQueries;
    List<String> expandedKeywords;

    public QueryRewriteResult(String originalQuery, String rewrittenQuery,
                             List<String> decomposedQueries, List<String> expandedKeywords) {
        this.originalQuery = originalQuery;
        this.rewrittenQuery = rewrittenQuery != null ? rewrittenQuery : originalQuery;
        this.decomposedQueries = decomposedQueries != null ? decomposedQueries : List.of(originalQuery);
        this.expandedKeywords = expandedKeywords != null ? expandedKeywords : List.of(originalQuery);
    }
}