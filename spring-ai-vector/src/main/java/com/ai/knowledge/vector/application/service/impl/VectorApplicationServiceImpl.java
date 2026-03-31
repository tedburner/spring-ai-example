package com.ai.knowledge.vector.application.service.impl;

import com.ai.knowledge.vector.application.service.VectorApplicationService;
import com.ai.knowledge.vector.domain.rag.metadata.AdvancedFilterDomainService;
import com.ai.knowledge.vector.domain.rag.metadata.DocumentMetadata;
import com.ai.knowledge.vector.domain.rag.metadata.FilterCriteria;
import com.ai.knowledge.vector.domain.rag.metadata.MetadataEnrichmentDomainService;
import com.ai.knowledge.vector.domain.rag.query.QueryRewriteDomainService;
import com.ai.knowledge.vector.domain.rag.query.QueryRewriteResult;
import com.ai.knowledge.vector.domain.rag.query.SearchQuery;
import com.ai.knowledge.vector.domain.vector.entity.VectorStoreResultDTO;
import com.ai.knowledge.vector.domain.vector.repository.VectorStoreRepository;
import com.ai.knowledge.vector.domain.vector.service.EmbeddingTextService;
import com.ai.knowledge.vector.domain.vector.service.HybridSearchService;
import com.ai.knowledge.vector.interfaces.assembler.VectorStoreResultAssembler;
import com.ai.knowledge.vector.interfaces.vo.vector.VectorStoreResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 向量应用服务实现
 * 遵循DDD规范，协调领域服务完成业务用例
 *
 * @author kiturone
 * @date 2025/5/11 21:13
 */
@Service
public class VectorApplicationServiceImpl implements VectorApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VectorApplicationServiceImpl.class);

    private final EmbeddingTextService embeddingTextService;
    private final VectorStoreRepository vectorStoreRepository;
    private final HybridSearchService hybridSearchService;
    private final QueryRewriteDomainService queryRewriteDomainService;
    private final MetadataEnrichmentDomainService metadataEnrichmentDomainService;
    private final AdvancedFilterDomainService advancedFilterDomainService;

    public VectorApplicationServiceImpl(EmbeddingTextService embeddingTextService,
                                       VectorStoreRepository vectorStoreRepository,
                                       HybridSearchService hybridSearchService,
                                       QueryRewriteDomainService queryRewriteDomainService,
                                       MetadataEnrichmentDomainService metadataEnrichmentDomainService,
                                       AdvancedFilterDomainService advancedFilterDomainService) {
        this.embeddingTextService = embeddingTextService;
        this.vectorStoreRepository = vectorStoreRepository;
        this.hybridSearchService = hybridSearchService;
        this.queryRewriteDomainService = queryRewriteDomainService;
        this.metadataEnrichmentDomainService = metadataEnrichmentDomainService;
        this.advancedFilterDomainService = advancedFilterDomainService;
    }

    @Override
    public List<Float> embedding(String text) {
        final float[] embedding = embeddingTextService.embedding(text);
        return IntStream.range(0, embedding.length)
                .mapToObj(i -> embedding[i])
                .toList();
    }

    @Override
    public VectorStoreResultVO store(String text) {
        // 文本进行向量化
        float[] embedding = embeddingTextService.embedding(text);
        LOGGER.info("文本向量化成功：{}", embedding.length);

        // 构建向量存储对象
        final VectorStoreResultDTO result = vectorStoreRepository.store(text, embedding);
        return VectorStoreResultAssembler.INSTANCE.toVo(result);
    }

    @Override
    public VectorStoreResultVO autoStore(String text) {
        // 调用 spring ai 框架自行进行存储
        final VectorStoreResultDTO result = vectorStoreRepository.store(text);
        return VectorStoreResultAssembler.INSTANCE.toVo(result);
    }

    @Override
    public List<Document> retrieval(String text, Integer topK, double threshold) {
        return vectorStoreRepository.retrieval(text, topK, threshold);
    }

    @Override
    public List<Document> hybridRetrieval(String query, Integer topK,
                                          Double semanticWeight, Double keywordWeight,
                                          Map<String, Object> filters) {
        // 设置默认值
        double semWeight = semanticWeight != null ? semanticWeight : 0.7;
        double keyWeight = keywordWeight != null ? keywordWeight : 0.3;
        int k = topK != null ? topK : 5;
        Map<String, Object> filterMap = filters != null ? filters : Map.of();

        return hybridSearchService.hybridSearchWithFilters(query, k, filterMap, semWeight, keyWeight);
    }

    @Override
    public String rewriteQuery(String query) {
        SearchQuery searchQuery = new SearchQuery(query);
        SearchQuery rewrittenQuery = queryRewriteDomainService.rewriteQuery(searchQuery);
        return rewrittenQuery.getRewrittenQuery();
    }

    @Override
    public String[] decomposeQuery(String query) {
        SearchQuery searchQuery = new SearchQuery(query);
        QueryRewriteResult result = queryRewriteDomainService.decomposeQuery(searchQuery);
        return result.getDecomposedQueries().toArray(new String[0]);
    }

    @Override
    public String[] expandKeywords(String query) {
        SearchQuery searchQuery = new SearchQuery(query);
        QueryRewriteResult result = queryRewriteDomainService.expandKeywords(searchQuery);
        return result.getExpandedKeywords().toArray(new String[0]);
    }

    @Override
    public List<Document> hybridRetrievalWithRewrite(String query, Integer topK,
                                                    Double semanticWeight, Double keywordWeight,
                                                    Map<String, Object> filters) {
        // 首先改写查询
        String rewrittenQuery = rewriteQuery(query);
        LOGGER.info("查询已改写: {} -> {}", query, rewrittenQuery);

        // 使用改写后的查询进行混合检索
        return hybridRetrieval(rewrittenQuery, topK, semanticWeight, keywordWeight, filters);
    }

    @Override
    public List<Document> retrievalWithMetadata(String query, Integer topK, String author, String category, String source) {
        // 先执行基本的混合检索
        List<Document> initialResults = hybridRetrieval(query, topK, null, null, Map.of());

        // 然后对结果进行元数据过滤
        FilterCriteria filterCriteria = new FilterCriteria(
            author,
            category != null ? List.of(category) : List.of(),
            List.of(), // 标签为空
            null, // 开始日期
            null, // 结束日期
            null, // 最低置信度
            source,
            Map.of() // 自定义属性为空
        );

        return advancedFilterDomainService.applyFilters(initialResults, filterCriteria);
    }

    @Override
    public List<Document> advancedFilteredRetrieval(String query, Integer topK, Map<String, Object> filterCriteria) {
        // 先执行基本的混合检索
        List<Document> initialResults = hybridRetrieval(query, topK, null, null, Map.of());

        // 从map构建FilterCriteria对象
        FilterCriteria criteria = buildFilterCriteriaFromMap(filterCriteria);

        // 应用高级过滤
        return advancedFilterDomainService.applyFilters(initialResults, criteria);
    }

    /**
     * 从Map构建FilterCriteria对象
     */
    private FilterCriteria buildFilterCriteriaFromMap(Map<String, Object> filterMap) {
        String author = (String) filterMap.getOrDefault("author", null);
        List<String> categories = (List<String>) filterMap.getOrDefault("categories", List.of());
        List<String> tags = (List<String>) filterMap.getOrDefault("tags", List.of());
        LocalDateTime dateFrom = (LocalDateTime) filterMap.getOrDefault("dateFrom", null);
        LocalDateTime dateTo = (LocalDateTime) filterMap.getOrDefault("dateTo", null);
        Double minConfidence = (Double) filterMap.getOrDefault("minConfidence", null);
        String source = (String) filterMap.getOrDefault("source", null);
        Map<String, Object> customProperties = (Map<String, Object>) filterMap.getOrDefault("customProperties", Map.of());

        return new FilterCriteria(author, categories, tags, dateFrom, dateTo, minConfidence, source, customProperties);
    }
}