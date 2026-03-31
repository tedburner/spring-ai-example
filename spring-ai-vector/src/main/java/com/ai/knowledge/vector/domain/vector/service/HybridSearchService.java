package com.ai.knowledge.vector.domain.vector.service;

import org.springframework.ai.document.Document;
import java.util.List;

/**
 * 混合搜索服务接口
 */
public interface HybridSearchService {

    /**
     * 混合搜索 - 结合向量搜索和关键词搜索
     *
     * @param query 查询字符串
     * @param topK 返回结果数量
     * @param semanticWeight 语义搜索权重 (0.0-1.0)
     * @param keywordWeight 关键词搜索权重 (0.0-1.0)
     * @return 搜索结果列表
     */
    List<Document> hybridSearch(String query, int topK, double semanticWeight, double keywordWeight);

    /**
     * 基于过滤条件的混合搜索
     *
     * @param query 查询字符串
     * @param topK 返回结果数量
     * @param filters 元数据过滤条件
     * @param semanticWeight 语义搜索权重
     * @param keywordWeight 关键词搜索权重
     * @return 搜索结果列表
     */
    List<Document> hybridSearchWithFilters(String query, int topK,
                                          java.util.Map<String, Object> filters,
                                          double semanticWeight, double keywordWeight);
}