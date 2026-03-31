package com.ai.knowledge.vector.application.service.impl;

import com.ai.knowledge.vector.domain.rag.metadata.AdvancedFilterDomainService;
import com.ai.knowledge.vector.domain.rag.metadata.DocumentMetadata;
import com.ai.knowledge.vector.domain.rag.metadata.FilterCriteria;
import com.ai.knowledge.vector.domain.rag.metadata.MetadataEnrichmentDomainService;
import com.ai.knowledge.vector.domain.rag.query.QueryRewriteDomainService;
import com.ai.knowledge.vector.domain.vector.repository.VectorStoreRepository;
import com.ai.knowledge.vector.domain.vector.service.EmbeddingTextService;
import com.ai.knowledge.vector.domain.vector.service.HybridSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VectorApplicationServiceImplTest {

    @Mock
    private EmbeddingTextService embeddingTextService;

    @Mock
    private VectorStoreRepository vectorStoreRepository;

    @Mock
    private HybridSearchService hybridSearchService;

    @Mock
    private QueryRewriteDomainService queryRewriteDomainService;

    @Mock
    private MetadataEnrichmentDomainService metadataEnrichmentDomainService;

    @Mock
    private AdvancedFilterDomainService advancedFilterDomainService;

    private VectorApplicationServiceImpl vectorApplicationService;

    @BeforeEach
    void setUp() {
        vectorApplicationService = new VectorApplicationServiceImpl(
            embeddingTextService,
            vectorStoreRepository,
            hybridSearchService,
            queryRewriteDomainService,
            metadataEnrichmentDomainService,
            advancedFilterDomainService
        );
    }

    @Test
    void testRetrievalWithMetadata() {
        // Given
        String query = "test query";
        List<Document> mockResults = List.of(new Document("test content"));
        List<Document> filteredResults = List.of(new Document("filtered content"));

        when(hybridSearchService.hybridSearchWithFilters(
            eq(query), eq(5), any(Map.class), eq(0.7), eq(0.3)
        )).thenReturn(mockResults);

        when(advancedFilterDomainService.applyFilters(any(List.class), any(FilterCriteria.class)))
            .thenReturn(filteredResults);

        // When
        List<Document> result = vectorApplicationService.retrievalWithMetadata(query, 5, "author1", "tech", "source1");

        // Then
        assertEquals(filteredResults, result);
    }

    @Test
    void testAdvancedFilteredRetrieval() {
        // Given
        String query = "advanced query";
        List<Document> mockResults = List.of(new Document("test content"));
        List<Document> filteredResults = List.of(new Document("filtered content"));

        Map<String, Object> filterMap = Map.of(
            "author", "test-author",
            "categories", List.of("tech", "business")
        );

        when(hybridSearchService.hybridSearchWithFilters(
            eq(query), eq(5), any(Map.class), eq(0.7), eq(0.3)
        )).thenReturn(mockResults);

        when(advancedFilterDomainService.applyFilters(any(List.class), any(FilterCriteria.class)))
            .thenReturn(filteredResults);

        // When
        List<Document> result = vectorApplicationService.advancedFilteredRetrieval(query, 5, filterMap);

        // Then
        assertEquals(filteredResults, result);
    }
}