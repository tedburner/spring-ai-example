package com.ai.knowledge.vector.application.service;

import com.ai.knowledge.vector.interfaces.vo.vector.VectorStoreResultVO;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 向量应用服务接口
 * 遵循DDD规范，协调领域服务完成业务用例
 */
public interface VectorApplicationService {

    /**
     * 文本向量化接口
     *
     * @param text 文本
     * @return 向量
     */
    List<Float> embedding(String text);

    /**
     * 单条文本进行向量存储接口
     *
     * @param text 文本
     * @return 向量化结果
     */
    VectorStoreResultVO store(String text);

    /**
     * 单条文本进行向量存储接口(自动存储)
     *
     * @param text 文本
     * @return 向量化结果
     */
    VectorStoreResultVO autoStore(String text);

    /**
     * 向量检索
     *
     * @param text      问句文本
     * @param topK      检索top k
     * @param threshold 阈值
     * @return 检索答案
     */
    List<Document> retrieval(String text, Integer topK, double threshold);

    /**
     * 混合检索
     *
     * @param query 查询文本
     * @param topK      检索top k
     * @param semanticWeight 语义搜索权重
     * @param keywordWeight 关键词搜索权重
     * @param filters 过滤条件
     * @return 检索答案
     */
    List<Document> hybridRetrieval(String query, Integer topK,
                                   Double semanticWeight, Double keywordWeight,
                                   Map<String, Object> filters);

    /**
     * 查询改写
     *
     * @param query 原始查询
     * @return 改写后的查询
     */
    String rewriteQuery(String query);

    /**
     * 查询分解
     *
     * @param query 复杂查询
     * @return 分解的子查询数组
     */
    String[] decomposeQuery(String query);

    /**
     * 关键词扩展
     *
     * @param query 原始查询
     * @return 扩展的关键词数组
     */
    String[] expandKeywords(String query);

    /**
     * 使用改写后的查询进行混合检索
     *
     * @param query 查询文本
     * @param topK      检索top k
     * @param semanticWeight 语义搜索权重
     * @param keywordWeight 关键词搜索权重
     * @param filters 过滤条件
     * @return 检索答案
     */
    List<Document> hybridRetrievalWithRewrite(String query, Integer topK,
                                              Double semanticWeight, Double keywordWeight,
                                              Map<String, Object> filters);

    /**
     * 元数据增强文档检索
     *
     * @param query 查询文本
     * @param topK  检索top k
     * @param author 作者过滤
     * @param category 分类过滤
     * @param source 来源过滤
     * @return 检索答案
     */
    List<Document> retrievalWithMetadata(String query, Integer topK, String author, String category, String source);

    /**
     * 高级过滤检索
     *
     * @param query 查询文本
     * @param topK 检索top k
     * @param filterCriteria 过滤条件
     * @return 检索答案
     */
    List<Document> advancedFilteredRetrieval(String query, Integer topK, Map<String, Object> filterCriteria);
}