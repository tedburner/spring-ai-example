package com.ai.knowledge.vector.domain.rag.metadata.impl;

import com.ai.knowledge.vector.domain.rag.metadata.DocumentMetadata;
import com.ai.knowledge.vector.domain.rag.metadata.MetadataEnrichmentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 元数据丰富领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataEnrichmentDomainServiceImpl implements MetadataEnrichmentDomainService {

    @Override
    public Document enrichWithMetadata(Document document, DocumentMetadata metadata) {
        log.debug("为文档 {} 添加元数据", document.getId());

        // 将新元数据与现有元数据合并
        Map<String, Object> mergedMetadata = mergeMetadata(document.getMetadata(), metadata);

        // 使用文档的原始内容和合并后的元数据创建新文档
        return new Document(document.getFormattedContent(), mergedMetadata);
    }

    @Override
    public List<Document> enrichDocumentsWithMetadata(List<Document> documents, List<DocumentMetadata> metadataList) {
        log.debug("批量为 {} 个文档添加元数据", documents.size());

        if (documents.size() != metadataList.size()) {
            throw new IllegalArgumentException("文档数量和元数据数量不匹配");
        }

        return IntStream.range(0, documents.size())
                .mapToObj(i -> enrichWithMetadata(documents.get(i), metadataList.get(i)))
                .toList();
    }

    /**
     * 合并文档现有元数据与新的元数据
     */
    private Map<String, Object> mergeMetadata(Map<String, Object> existingMetadata, DocumentMetadata newMetadata) {
        // 如果现有元数据为空，创建一个新map
        Map<String, Object> result = existingMetadata != null ?
            new java.util.HashMap<>(existingMetadata) : new java.util.HashMap<String, Object>();

        // 添加新元数据
        result.put("author", newMetadata.getAuthor());
        result.put("creation_date", newMetadata.getCreationDate());
        result.put("last_modified", newMetadata.getLastModified());
        result.put("category", newMetadata.getCategory());
        result.put("source", newMetadata.getSource());
        result.put("tags", newMetadata.getTags());
        result.put("confidence_score", newMetadata.getConfidenceScore());
        result.put("custom_properties", newMetadata.getCustomProperties());

        return result;
    }
}