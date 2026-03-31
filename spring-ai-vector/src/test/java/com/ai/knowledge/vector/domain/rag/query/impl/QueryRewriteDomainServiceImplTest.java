package com.ai.knowledge.vector.domain.rag.query.impl;

import com.ai.knowledge.vector.domain.rag.query.SearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryRewriteDomainServiceImplTest {

    @Mock
    private ChatModel chatModel;

    private QueryRewriteDomainServiceImpl queryRewriteService;

    @BeforeEach
    void setUp() {
        queryRewriteService = new QueryRewriteDomainServiceImpl(chatModel);
    }

    @Test
    void testRewriteQuery() {
        // Given
        String originalQuery = "How to use Spring AI?";
        SearchQuery searchQuery = new SearchQuery(originalQuery);

        // Mock chat model response
        ChatResponse mockResponse = createMockChatResponse("Spring AI usage guide");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        SearchQuery result = queryRewriteService.rewriteQuery(searchQuery);

        // Then
        assertNotNull(result);
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals("Spring AI usage guide", result.getRewrittenQuery());
    }

    @Test
    void testRewriteQueryExceptionHandling() {
        // Given
        String originalQuery = "Exception test query";
        SearchQuery searchQuery = new SearchQuery(originalQuery);

        // Mock exception
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("AI service error"));

        // When
        SearchQuery result = queryRewriteService.rewriteQuery(searchQuery);

        // Then
        assertNotNull(result);
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals(originalQuery, result.getRewrittenQuery()); // Should return original on error
    }

    @Test
    void testDecomposeQuery() {
        // Given
        String originalQuery = "How to implement Spring AI with Elasticsearch and MongoDB?";
        SearchQuery searchQuery = new SearchQuery(originalQuery);

        // Mock chat model response
        ChatResponse mockResponse = createMockChatResponse("How to implement Spring AI\nHow to use Elasticsearch\nHow to use MongoDB");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        var result = queryRewriteService.decomposeQuery(searchQuery);

        // Then
        assertNotNull(result);
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals(3, result.getDecomposedQueries().size());
        assertTrue(result.getDecomposedQueries().contains("How to implement Spring AI"));
        assertTrue(result.getDecomposedQueries().contains("How to use Elasticsearch"));
        assertTrue(result.getDecomposedQueries().contains("How to use MongoDB"));
    }

    @Test
    void testExpandKeywords() {
        // Given
        String originalQuery = "machine learning";
        SearchQuery searchQuery = new SearchQuery(originalQuery);

        // Mock chat model response
        ChatResponse mockResponse = createMockChatResponse("artificial intelligence\nML\nneural networks\ndeep learning");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        var result = queryRewriteService.expandKeywords(searchQuery);

        // Then
        assertNotNull(result);
        assertEquals(originalQuery, result.getOriginalQuery());
        assertTrue(result.getExpandedKeywords().contains("machine learning")); // Original query should be first
        assertTrue(result.getExpandedKeywords().contains("artificial intelligence"));
        assertTrue(result.getExpandedKeywords().contains("ML"));
        assertTrue(result.getExpandedKeywords().contains("neural networks"));
        assertTrue(result.getExpandedKeywords().contains("deep learning"));
    }

    @Test
    void testCompleteRewrite() {
        // Given
        String originalQuery = "How to use Spring AI?";
        SearchQuery searchQuery = new SearchQuery(originalQuery);

        // Mock chat model responses
        when(chatModel.call(any(Prompt.class)))
            .thenReturn(createMockChatResponse("How to use Spring AI effectively"))
            .thenReturn(createMockChatResponse("How to use Spring AI"))
            .thenReturn(createMockChatResponse("Spring AI\nSpring Framework\nAI integration"));

        // When
        var result = queryRewriteService.completeRewrite(searchQuery);

        // Then
        assertNotNull(result);
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals("How to use Spring AI effectively", result.getRewrittenQuery());
        assertNotNull(result.getDecomposedQueries());
        assertNotNull(result.getExpandedKeywords());
    }

    // Helper method to create mock ChatResponse
    private ChatResponse createMockChatResponse(String responseText) {
        Generation mockGeneration = new Generation(new AssistantMessage(responseText));
        return new ChatResponse(List.of(mockGeneration));
    }
}