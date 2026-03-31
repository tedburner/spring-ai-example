package com.ai.knowledge.vector.domain.rag.metadata.impl;

import com.ai.knowledge.vector.domain.rag.metadata.DocumentMetadata;
import com.ai.knowledge.vector.domain.rag.metadata.FilterCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdvancedFilterDomainServiceImplTest {

    private AdvancedFilterDomainServiceImpl filterService;

    @BeforeEach
    void setUp() {
        filterService = new AdvancedFilterDomainServiceImpl();
    }

    @Test
    void testApplyFiltersWithAuthor() {
        // Given
        Document doc1 = new Document("Content 1", Map.of("author", "John Doe"));
        Document doc2 = new Document("Content 2", Map.of("author", "Jane Smith"));
        List<Document> documents = List.of(doc1, doc2);

        FilterCriteria criteria = new FilterCriteria("John Doe", List.of(), List.of(),
                null, null, null, null, Map.of());

        // When
        List<Document> results = filterService.applyFilters(documents, criteria);

        // Then
        assertEquals(1, results.size());
        assertEquals("Content 1", results.get(0).getFormattedContent());
    }

    @Test
    void testMatchesFilterWithCategory() {
        // Given
        Document document = new Document("Test content", Map.of(
            "category", "technical",
            "author", "John Doe"
        ));

        FilterCriteria criteria = new FilterCriteria(null, List.of("technical", "business"), List.of(),
                null, null, null, null, Map.of());

        // When
        boolean matches = filterService.matchesFilter(document, criteria);

        // Then
        assertTrue(matches);
    }

    @Test
    void testMatchesFilterWithNonMatchingCategory() {
        // Given
        Document document = new Document("Test content", Map.of(
            "category", "financial",
            "author", "John Doe"
        ));

        FilterCriteria criteria = new FilterCriteria(null, List.of("technical", "business"), List.of(),
                null, null, null, null, Map.of());

        // When
        boolean matches = filterService.matchesFilter(document, criteria);

        // Then
        assertFalse(matches);
    }

    @Test
    void testApplyFiltersWithDateRange() {
        // Given
        LocalDateTime date1 = LocalDateTime.of(2023, 1, 15, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2023, 3, 15, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2023, 6, 15, 10, 0);

        Document doc1 = new Document("Old content", Map.of("creation_date", date1));
        Document doc2 = new Document("Middle content", Map.of("creation_date", date2));
        Document doc3 = new Document("New content", Map.of("creation_date", date3));

        List<Document> documents = List.of(doc1, doc2, doc3);

        FilterCriteria criteria = new FilterCriteria(null, List.of(), List.of(),
                LocalDateTime.of(2023, 2, 1, 0, 0), LocalDateTime.of(2023, 4, 1, 0, 0), null, null, Map.of());

        // When
        List<Document> results = filterService.applyFilters(documents, criteria);

        // Then
        assertEquals(1, results.size());
        assertEquals("Middle content", results.get(0).getFormattedContent());
    }

    @Test
    void testApplyFiltersWithConfidenceScore() {
        // Given
        Document doc1 = new Document("Low confidence", Map.of("confidence_score", 0.3));
        Document doc2 = new Document("High confidence", Map.of("confidence_score", 0.8));
        List<Document> documents = List.of(doc1, doc2);

        FilterCriteria criteria = new FilterCriteria(null, List.of(), List.of(),
                null, null, 0.5, null, Map.of());

        // When
        List<Document> results = filterService.applyFilters(documents, criteria);

        // Then
        assertEquals(1, results.size());
        assertEquals("High confidence", results.get(0).getFormattedContent());
    }

    @Test
    void testApplyFiltersWithCustomProperties() {
        // Given
        Map<String, Object> customProps1 = Map.of("department", "engineering", "level", "senior");
        Map<String, Object> customProps2 = Map.of("department", "marketing", "level", "junior");

        Document doc1 = new Document("Engineering doc", Map.of("custom_properties", customProps1));
        Document doc2 = new Document("Marketing doc", Map.of("custom_properties", customProps2));
        List<Document> documents = List.of(doc1, doc2);

        Map<String, Object> filterCustomProps = Map.of("department", "engineering");
        FilterCriteria criteria = new FilterCriteria(null, List.of(), List.of(),
                null, null, null, null, filterCustomProps);

        // When
        List<Document> results = filterService.applyFilters(documents, criteria);

        // Then
        assertEquals(1, results.size());
        assertEquals("Engineering doc", results.get(0).getFormattedContent());
    }

    @Test
    void testMatchesFilterNoCriteria() {
        // Given
        Document document = new Document("Test content", Map.of("author", "John"));

        FilterCriteria criteria = new FilterCriteria(null, List.of(), List.of(),
                null, null, null, null, Map.of());

        // When
        boolean matches = filterService.matchesFilter(document, criteria);

        // Then
        assertTrue(matches); // Should match when no criteria specified
    }
}