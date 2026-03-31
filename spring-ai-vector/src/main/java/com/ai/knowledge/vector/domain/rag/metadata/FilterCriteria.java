package com.ai.knowledge.vector.domain.rag.metadata;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 过滤条件值对象 - 表示搜索过滤的条件
 */
@Value
public class FilterCriteria {
    String author;
    List<String> categories;
    List<String> tags;
    LocalDateTime dateFrom;
    LocalDateTime dateTo;
    Double minConfidence;
    String source;
    Map<String, Object> customProperties;

    public FilterCriteria(String author, List<String> categories, List<String> tags,
                         LocalDateTime dateFrom, LocalDateTime dateTo, Double minConfidence,
                         String source, Map<String, Object> customProperties) {
        this.author = author;
        this.categories = categories != null ? categories : List.of();
        this.tags = tags != null ? tags : List.of();
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.minConfidence = minConfidence != null ? Math.max(0.0, Math.min(1.0, minConfidence)) : null;
        this.source = source;
        this.customProperties = customProperties != null ? customProperties : Map.of();
    }
}