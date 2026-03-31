package com.ai.knowledge.vector.domain.vector.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HybridSearchServiceImplTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private ElasticsearchVectorStore elasticsearchVectorStore;

    private HybridSearchServiceImpl hybridSearchService;

    @BeforeEach
    void setUp() {
        hybridSearchService = new HybridSearchServiceImpl(elasticsearchClient, elasticsearchVectorStore);
    }

    @Test
    void testHybridSearchBasic() {
        // Given
        String query = "test query";
        int topK = 5;
        double semanticWeight = 0.7;
        double keywordWeight = 0.3;

        // Mock vector store response
        List<Document> semanticResults = Collections.singletonList(
            new Document("test content for query")
        );
        when(elasticsearchVectorStore.similaritySearch(any(org.springframework.ai.vectorstore.SearchRequest.class))).thenReturn(semanticResults);

        // When
        List<Document> results = hybridSearchService.hybridSearch(query, topK, semanticWeight, keywordWeight);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testHybridSearchWithEmptyFilters() {
        // Given
        String query = "another test query";
        int topK = 3;
        double semanticWeight = 0.6;
        double keywordWeight = 0.4;

        // When
        List<Document> results = hybridSearchService.hybridSearchWithFilters(
            query, topK, Collections.emptyMap(), semanticWeight, keywordWeight
        );

        // Then
        assertNotNull(results);
    }

    @Test
    void testPerformFallbackSearch() throws IOException {
        // Given
        String query = "fallback test";
        int topK = 5;

        // Mock ES client to throw exception to trigger fallback
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), any(Class.class))).thenThrow(new IOException("ES unavailable"));

        // When
        List<Document> results = hybridSearchService.hybridSearch(query, topK, 0.5, 0.5);

        // Then
        assertNotNull(results);
    }
}