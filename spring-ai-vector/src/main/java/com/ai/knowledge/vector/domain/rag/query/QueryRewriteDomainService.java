package com.ai.knowledge.vector.domain.rag.query;

/**
 * 查询改写领域服务接口
 * 负责查询的理解、改写、分解和扩展
 */
public interface QueryRewriteDomainService {

    /**
     * 改写查询语句以提高检索效果
     *
     * @param searchQuery 原始查询对象
     * @return 改写后的查询对象
     */
    SearchQuery rewriteQuery(SearchQuery searchQuery);

    /**
     * 将复杂查询分解为多个子查询
     *
     * @param searchQuery 复杂查询
     * @return 查询改写结果，包含分解的子查询
     */
    QueryRewriteResult decomposeQuery(SearchQuery searchQuery);

    /**
     * 扩展查询关键词
     *
     * @param searchQuery 原始查询
     * @return 查询改写结果，包含扩展的关键词
     */
    QueryRewriteResult expandKeywords(SearchQuery searchQuery);

    /**
     * 完整的查询改写流程，包含改写、分解和扩展
     *
     * @param searchQuery 原始查询
     * @return 完整的查询改写结果
     */
    QueryRewriteResult completeRewrite(SearchQuery searchQuery);
}