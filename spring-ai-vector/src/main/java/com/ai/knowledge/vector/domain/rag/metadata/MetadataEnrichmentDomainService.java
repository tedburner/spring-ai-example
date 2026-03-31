package com.ai.knowledge.vector.domain.rag.metadata;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 元数据丰富领域服务接口
 */
public interface MetadataEnrichmentDomainService {

    /**
     * 为文档添加元数据
     *
     * @param document 原始文档
     * @param metadata 元数据
     * @return 添加元数据后的文档
     */
    Document enrichWithMetadata(Document document, DocumentMetadata metadata);

    /**
     * 批量为文档添加元数据
     *
     * @param documents 原始文档列表
     * @param metadataList 元数据列表
     * @return 添加元数据后的文档列表
     */
    List<Document> enrichDocumentsWithMetadata(List<Document> documents, List<DocumentMetadata> metadataList);
}