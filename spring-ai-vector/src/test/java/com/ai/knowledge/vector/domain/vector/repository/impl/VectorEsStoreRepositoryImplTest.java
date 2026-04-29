package com.ai.knowledge.vector.domain.vector.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * VectorEsStoreRepositoryImpl 单元测试（含 retrieval 修复 + 批量存储验证）
 */
@ExtendWith(MockitoExtension.class)
class VectorEsStoreRepositoryImplTest {

    @Mock
    private co.elastic.clients.elasticsearch.ElasticsearchClient elasticsearchClient;

    @Mock
    private ElasticsearchVectorStore elasticsearchVectorStore;

    @Mock
    private ElasticsearchVectorStoreProperties options;

    private VectorEsStoreRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new VectorEsStoreRepositoryImpl(
                elasticsearchClient, elasticsearchVectorStore, options);
    }

    @Test
    void testStoreSingleText() {
        var result = repository.store("测试文本");

        assertNotNull(result);
        assertEquals(1, result.getSuccess());
        assertEquals(0, result.getFail());
        verify(elasticsearchVectorStore).add(anyList());
    }

    @Test
    void testStoreBatchDocuments() {
        List<Document> docs = List.of(
                new Document("文档1"),
                new Document("文档2"),
                new Document("文档3")
        );

        var result = repository.store(docs);

        assertNotNull(result);
        assertEquals(3, result.getSuccess());
        assertEquals(0, result.getFail());
        verify(elasticsearchVectorStore).add(docs);
    }

    @Test
    void testStoreBatchWithException() {
        List<Document> docs = List.of(new Document("文档"));
        doThrow(new RuntimeException("ES不可用")).when(elasticsearchVectorStore).add(anyList());

        var result = repository.store(docs);

        assertNotNull(result);
        assertEquals(0, result.getSuccess());
        assertEquals(1, result.getFail());
    }

    @Test
    void testRetrievalUsesQueryNotFilter() {
        List<Document> expected = List.of(
                new Document("匹配结果1"),
                new Document("匹配结果2")
        );
        when(elasticsearchVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(expected);

        List<Document> results = repository.retrieval("搜索查询", 5, 0.5);

        assertEquals(2, results.size());
        verify(elasticsearchVectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testRetrievalWithDefaults() {
        List<Document> expected = List.of(new Document("默认结果"));
        when(elasticsearchVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(expected);

        // topK 为 null 时应使用默认值 5
        List<Document> results = repository.retrieval("查询", null, 0.5);

        assertEquals(1, results.size());
    }

    @Test
    void testRetrievalWithScore() {
        List<Document> expected = List.of(new Document("带分数结果"));
        when(elasticsearchVectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(expected);

        List<Document> results = repository.retrievalWithScore("查询", 3, 0.7);

        assertEquals(1, results.size());
        verify(elasticsearchVectorStore).similaritySearch(any(SearchRequest.class));
    }
}
