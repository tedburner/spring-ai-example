package com.ai.knowledge.vector.domain.rag.answer;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 基于检索文档生成答案的领域服务。
 */
public interface AnswerGenerationDomainService {

    /**
     * 根据检索到的文档生成回答
     */
    String generateAnswer(String query, List<Document> retrievedDocuments);
}
