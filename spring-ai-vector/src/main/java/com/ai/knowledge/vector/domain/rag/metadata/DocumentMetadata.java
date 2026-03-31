package com.ai.knowledge.vector.domain.rag.metadata;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文档元数据实体 - 表示文档的元数据信息
 */
@Value
public class DocumentMetadata {
    String id;
    String documentId;
    String author;
    LocalDateTime creationDate;
    LocalDateTime lastModified;
    String category;
    String source;
    Map<String, Object> tags;
    Map<String, Object> customProperties;
    double confidenceScore;

    public DocumentMetadata(String id, String documentId, String author, LocalDateTime creationDate,
                          LocalDateTime lastModified, String category, String source,
                          Map<String, Object> tags, Map<String, Object> customProperties, double confidenceScore) {
        this.id = id;
        this.documentId = documentId;
        this.author = author;
        this.creationDate = creationDate != null ? creationDate : LocalDateTime.now();
        this.lastModified = lastModified != null ? lastModified : this.creationDate;
        this.category = category;
        this.source = source;
        this.tags = tags != null ? tags : Map.of();
        this.customProperties = customProperties != null ? customProperties : Map.of();
        this.confidenceScore = Math.max(0.0, Math.min(1.0, confidenceScore)); // 确保在0-1范围内
    }

    /**
     * 创建更新的元数据实例
     */
    public DocumentMetadata withLastModified(LocalDateTime lastModified) {
        return new DocumentMetadata(
            this.id, this.documentId, this.author, this.creationDate,
            lastModified, this.category, this.source, this.tags,
            this.customProperties, this.confidenceScore
        );
    }
}