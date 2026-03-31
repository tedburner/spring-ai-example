package com.ai.knowledge.vector.domain.rag.metadata.impl;

import com.ai.knowledge.vector.domain.rag.metadata.AdvancedFilterDomainService;
import com.ai.knowledge.vector.domain.rag.metadata.FilterCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 高级过滤领域服务实现
 */
@Slf4j
@Service
public class AdvancedFilterDomainServiceImpl implements AdvancedFilterDomainService {

    @Override
    public List<Document> applyFilters(List<Document> documents, FilterCriteria filterCriteria) {
        log.debug("应用过滤条件到 {} 个文档", documents.size());

        Predicate<Document> filterPredicate = createFilterPredicate(filterCriteria);

        return documents.stream()
                .filter(filterPredicate)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matchesFilter(Document document, FilterCriteria filterCriteria) {
        return createFilterPredicate(filterCriteria).test(document);
    }

    /**
     * 创建过滤谓词
     */
    private Predicate<Document> createFilterPredicate(FilterCriteria filterCriteria) {
        return document -> {
            Map<String, Object> metadata = document.getMetadata();

            // 检查作者
            if (filterCriteria.getAuthor() != null) {
                Object author = metadata.get("author");
                if (author == null || !filterCriteria.getAuthor().equals(author.toString())) {
                    return false;
                }
            }

            // 检查分类
            if (!filterCriteria.getCategories().isEmpty()) {
                Object category = metadata.get("category");
                if (category == null || !filterCriteria.getCategories().contains(category.toString())) {
                    return false;
                }
            }

            // 检查标签
            if (!filterCriteria.getTags().isEmpty()) {
                Object tagsObj = metadata.get("tags");
                if (tagsObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tags = (Map<String, Object>) tagsObj;
                    boolean hasMatchingTag = filterCriteria.getTags().stream()
                            .anyMatch(tag -> tags.containsKey(tag) || tags.containsValue(tag));
                    if (!hasMatchingTag) {
                        return false;
                    }
                } else {
                    return false; // 如果tags不是map类型，则不匹配
                }
            }

            // 检查日期范围
            if (filterCriteria.getDateFrom() != null || filterCriteria.getDateTo() != null) {
                Object creationDate = metadata.get("creation_date");
                if (creationDate instanceof LocalDateTime) {
                    LocalDateTime docDate = (LocalDateTime) creationDate;
                    if (filterCriteria.getDateFrom() != null && docDate.isBefore(filterCriteria.getDateFrom())) {
                        return false;
                    }
                    if (filterCriteria.getDateTo() != null && docDate.isAfter(filterCriteria.getDateTo())) {
                        return false;
                    }
                } else {
                    // 如果日期不可比较，默认视为不匹配
                    return false;
                }
            }

            // 检查最低置信度
            if (filterCriteria.getMinConfidence() != null) {
                Object confidence = metadata.get("confidence_score");
                if (confidence instanceof Number) {
                    double confValue = ((Number) confidence).doubleValue();
                    if (confValue < filterCriteria.getMinConfidence()) {
                        return false;
                    }
                } else {
                    return false; // 如果置信度不是数值类型，则不匹配
                }
            }

            // 检查来源
            if (filterCriteria.getSource() != null) {
                Object source = metadata.get("source");
                if (source == null || !filterCriteria.getSource().equals(source.toString())) {
                    return false;
                }
            }

            // 检查自定义属性
            if (!filterCriteria.getCustomProperties().isEmpty()) {
                Object customProps = metadata.get("custom_properties");
                if (customProps instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> docCustomProps = (Map<String, Object>) customProps;
                    for (Map.Entry<String, Object> entry : filterCriteria.getCustomProperties().entrySet()) {
                        if (!docCustomProps.containsKey(entry.getKey()) ||
                            !docCustomProps.get(entry.getKey()).equals(entry.getValue())) {
                            return false;
                        }
                    }
                } else {
                    return false; // 如果自定义属性不是map类型，则不匹配
                }
            }

            return true; // 所有条件都通过
        };
    }
}