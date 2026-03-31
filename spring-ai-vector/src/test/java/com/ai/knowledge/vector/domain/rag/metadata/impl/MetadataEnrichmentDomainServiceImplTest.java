package com.ai.knowledge.vector.domain.rag.metadata.impl;

import com.ai.knowledge.vector.domain.rag.metadata.DocumentMetadata;
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
class MetadataEnrichmentDomainServiceImplTest {

    private MetadataEnrichmentDomainServiceImpl enrichmentService;

    @BeforeEach
    void setUp() {
        enrichmentService = new MetadataEnrichmentDomainServiceImpl();
    }

    @Test
    void testEnrichWithMetadata() {
        // Given
        Document document = new Document("Sample content");
        DocumentMetadata metadata = new DocumentMetadata(
            "meta-1", "doc-1", "John Doe",
            LocalDateTime.now(), LocalDateTime.now(),
            "technical", "source-1", Map.of("tag1", "value1"),
            Map.of("prop1", "value1"), 0.8
        );

        // When
        Document enrichedDoc = enrichmentService.enrichWithMetadata(document, metadata);

        // Then
        assertNotNull(enrichedDoc);
        assertEquals("Sample content", enrichedDoc.getFormattedContent());
        assertTrue(enrichedDoc.getMetadata().containsKey("author"));
        assertEquals("John Doe", enrichedDoc.getMetadata().get("author"));
        assertEquals("technical", enrichedDoc.getMetadata().get("category"));
        assertEquals(0.8, enrichedDoc.getMetadata().get("confidence_score"));
    }

    @Test
    void testEnrichWithMetadataNullCheck() {
        // Given
        Document document = new Document("Sample content", Map.of("existing", "value"));
        DocumentMetadata metadata = new DocumentMetadata(
            "meta-1", "doc-1", "Jane Smith",
            LocalDateTime.now(), LocalDateTime.now(),
            "business", "source-2", Map.of(),
            Map.of(), 0.9
        );

        // When
        Document enrichedDoc = enrichmentService.enrichWithMetadata(document, metadata);

        // Then
        assertNotNull(enrichedDoc);
        assertEquals("Sample content", enrichedDoc.getFormattedContent());
        assertEquals("Jane Smith", enrichedDoc.getMetadata().get("author"));
        assertEquals("business", enrichedDoc.getMetadata().get("category"));
    }

    @Test
    void testEnrichDocumentsWithMetadata() {
        // Given
        Document doc1 = new Document("Content 1");
        Document doc2 = new Document("Content 2");
        List<Document> documents = List.of(doc1, doc2);

        DocumentMetadata meta1 = new DocumentMetadata(
            "meta-1", "doc-1", "Author 1",
            LocalDateTime.now(), LocalDateTime.now(),
            "category-1", "source-1", Map.of("tag1", "val1"),
            Map.of("prop1", "val1"), 0.7
        );

        DocumentMetadata meta2 = new DocumentMetadata(
            "meta-2", "doc-2", "Author 2",
            LocalDateTime.now(), LocalDateTime.now(),
            "category-2", "source-2", Map.of("tag2", "val2"),
            Map.of("prop2", "val2"), 0.9
        );

        List<DocumentMetadata> metadataList = List.of(meta1, meta2);

        // When
        List<Document> enrichedDocs = enrichmentService.enrichDocumentsWithMetadata(documents, metadataList);

        // Then
        assertEquals(2, enrichedDocs.size());
        assertEquals("Author 1", enrichedDocs.get(0).getMetadata().get("author"));
        assertEquals("Author 2", enrichedDocs.get(1).getMetadata().get("author"));
        assertEquals("category-1", enrichedDocs.get(0).getMetadata().get("category"));
        assertEquals("category-2", enrichedDocs.get(1).getMetadata().get("category"));
    }

    @Test
    void testEnrichDocumentsWithMetadataMismatch() {
        // Given
        Document doc1 = new Document("Content 1");
        List<Document> documents = List.of(doc1);

        DocumentMetadata meta1 = new DocumentMetadata(
            "meta-1", "doc-1", "Author 1",
            LocalDateTime.now(), LocalDateTime.now(),
            "category-1", "source-1", Map.of(),
            Map.of(), 0.7
        );
        DocumentMetadata meta2 = new DocumentMetadata(
            "meta-2", "doc-2", "Author 2",
            LocalDateTime.now(), LocalDateTime.now(),
            "category-2", "source-2", Map.of(),
            Map.of(), 0.9
        );
        List<DocumentMetadata> metadataList = List.of(meta1, meta2); // More metadata than documents

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            enrichmentService.enrichDocumentsWithMetadata(documents, metadataList);
        });
    }
}