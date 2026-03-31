package com.ai.knowledge.vector.domain.vector.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ai.knowledge.vector.domain.vector.service.HybridSearchService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridSearchServiceImpl implements HybridSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchVectorStore elasticsearchVectorStore;
    private final String indexName = "ollama-rag-embedding-index";

    @Override
    public List<Document> hybridSearch(String query, int topK, double semanticWeight, double keywordWeight) {
        return hybridSearchWithFilters(query, topK, Collections.emptyMap(), semanticWeight, keywordWeight);
    }

    @Override
    public List<Document> hybridSearchWithFilters(String query, int topK,
                                                 Map<String, Object> filters,
                                                 double semanticWeight, double keywordWeight) {
        log.info("执行混合搜索，查询: {}, 语义权重: {}, 关键词权重: {}", query, semanticWeight, keywordWeight);

        try {
            // 执行向量搜索
            List<DocumentScorePair> semanticResults = performSemanticSearch(query, topK, filters);

            // 执行关键词搜索
            List<DocumentScorePair> keywordResults = performKeywordSearch(query, topK, filters);

            // 融合两种搜索结果
            Map<String, DocumentScorePair> combinedResults = new HashMap<>();

            // 添加语义搜索结果（乘以语义权重）
            for (DocumentScorePair pair : semanticResults) {
                combinedResults.put(pair.getDocument().getId(), // 使用ID作为key
                                  new DocumentScorePair(pair.getDocument(), pair.getScore() * semanticWeight));
            }

            // 合并关键词搜索结果（乘以关键词权重）
            for (DocumentScorePair pair : keywordResults) {
                DocumentScorePair existing = combinedResults.get(pair.getDocument().getId());
                if (existing != null) {
                    // 如果两个搜索都返回相同文档，则合并分数
                    double newScore = existing.getScore() + (pair.getScore() * keywordWeight);
                    combinedResults.put(pair.getDocument().getId(),
                                     new DocumentScorePair(pair.getDocument(), newScore));
                } else {
                    combinedResults.put(pair.getDocument().getId(),
                                     new DocumentScorePair(pair.getDocument(), pair.getScore() * keywordWeight));
                }
            }

            // 按综合得分排序
            return combinedResults.values().stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(topK)
                    .map(DocumentScorePair::getDocument)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("混合搜索执行失败: ", e);
            // 如果混合搜索失败，回退到纯向量搜索
            return performFallbackSearch(query, topK, filters);
        }
    }

    /**
     * 执行语义（向量）搜索
     */
    private List<DocumentScorePair> performSemanticSearch(String query, int topK, Map<String, Object> filters) {
        org.springframework.ai.vectorstore.SearchRequest searchRequest =
            org.springframework.ai.vectorstore.SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.1)
                .build();

        List<Document> results = elasticsearchVectorStore.similaritySearch(searchRequest);

        // 向量搜索返回的结果已经包含了相关性分数，但我们这里暂时使用默认值
        return results.stream()
                .map(doc -> new DocumentScorePair(doc, calculateSimilarityScore(doc))) // 基于距离计算实际分数
                .collect(Collectors.toList());
    }

    /**
     * 基于文档内容计算相似度分数（简化实现）
     */
    private double calculateSimilarityScore(Document doc) {
        // 这里可以根据实际情况实现更复杂的相似度评分逻辑
        // 临时实现，返回1.0作为默认值
        return 1.0;
    }

    /**
     * 执行关键词搜索
     */
    private List<DocumentScorePair> performKeywordSearch(String query, int topK, Map<String, Object> filters) {
        try {
            // 构建ES搜索请求
            co.elastic.clients.elasticsearch.core.SearchRequest esSearchRequest =
                co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> {
                    // 构建搜索查询
                    Query queryObj = Query.of(q -> {
                        // 创建布尔查询组合向量和关键词查询
                        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder boolQueryBuilder =
                            new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();

                        // 添加多字段匹配查询
                        boolQueryBuilder.must(m -> m
                            .multiMatch(mm -> mm
                                .query(query)
                                .fields("content", "content.*")
                                .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                                .tieBreaker(0.3)
                            ));

                        // 添加过滤条件
                        for (Map.Entry<String, Object> filter : filters.entrySet()) {
                            boolQueryBuilder.filter(f -> f
                                .term(t -> t
                                    .field(filter.getKey())
                                    .value(filter.getValue().toString())
                                ));
                        }

                        return q.bool(boolQueryBuilder.build());
                    });

                    return s
                        .index(indexName)
                        .query(queryObj)
                        .size(topK);
                });

            co.elastic.clients.elasticsearch.core.SearchResponse<Map> response =
                elasticsearchClient.search(esSearchRequest, Map.class);

            return response.hits().hits().stream()
                .map(hit -> {
                    Map<String, Object> source = hit.source();
                    String content = source.get("content").toString();
                    String id = hit.id();

                    // Spring AI的Document构造函数接收内容和其他元数据
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("id", id);
                    metadata.put("score", hit.score()); // 保存ES原始分数
                    if (source.containsKey("metadata")) {
                        metadata.putAll((Map<String, Object>) source.get("metadata"));
                    }
                    Document doc = new Document(content, metadata);

                    return new DocumentScorePair(doc, hit.score());
                })
                .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("关键词搜索执行失败: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * 回退搜索方法
     */
    private List<Document> performFallbackSearch(String query, int topK, Map<String, Object> filters) {
        log.info("执行回退搜索（纯向量搜索）");
        org.springframework.ai.vectorstore.SearchRequest searchRequest =
            org.springframework.ai.vectorstore.SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.1)
                .build();

        return elasticsearchVectorStore.similaritySearch(searchRequest);
    }

    /**
     * 内部类：文档及其分数的配对
     */
    @Getter
    private static class DocumentScorePair {
        private final Document document;
        private final double score;

        public DocumentScorePair(Document document, double score) {
            this.document = document;
            this.score = score;
        }

    }
}