package com.ai.knowledge.vector.domain.rag.metadata;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 高级过滤领域服务接口
 */
public interface AdvancedFilterDomainService {

    /**
     * 应用过滤条件到文档列表
     *
     * @param documents 原始文档列表
     * @param filterCriteria 过滤条件
     * @return 过滤后的文档列表
     */
    List<Document> applyFilters(List<Document> documents, FilterCriteria filterCriteria);

    /**
     * 检查单个文档是否符合过滤条件
     *
     * @param document 文档
     * @param filterCriteria 过滤条件
     * @return 是否符合条件
     */
    boolean matchesFilter(Document document, FilterCriteria filterCriteria);
}