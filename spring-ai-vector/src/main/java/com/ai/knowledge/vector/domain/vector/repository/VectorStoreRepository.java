package com.ai.knowledge.vector.domain.vector.repository;

import com.ai.knowledge.vector.domain.vector.entity.VectorStoreResultDTO;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @author kiturone
 * @date 2025/5/11 10:36
 */
public interface VectorStoreRepository {

    /**
     * 单条文本向量化存储
     */
    VectorStoreResultDTO store(String text);

    /**
     * 单条文本向量化存储（手动 embedding）
     */
    VectorStoreResultDTO store(String text, float[] embedding);

    /**
     * 批量文档向量化存储
     */
    VectorStoreResultDTO store(List<Document> documents);

    /**
     * 向量检索
     */
    List<Document> retrieval(String text, Integer topK, double threshold);

    /**
     * 向量检索（返回带相似度分数）
     */
    List<Document> retrievalWithScore(String text, Integer topK, double threshold);
}
