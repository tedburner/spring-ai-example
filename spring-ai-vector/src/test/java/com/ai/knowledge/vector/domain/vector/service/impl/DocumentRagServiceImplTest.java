package com.ai.knowledge.vector.domain.vector.service.impl;

import com.ai.knowledge.vector.domain.vector.entity.DocumentRagResultDTO;
import com.ai.knowledge.vector.domain.vector.entity.VectorStoreResultDTO;
import com.ai.knowledge.vector.domain.vector.repository.VectorStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * DocumentRagServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DocumentRagServiceImplTest {

    @Mock
    private VectorStoreRepository vectorStoreRepository;

    @Mock
    private PagePdfDocumentReader mockPdfReader;

    private TestableDocumentRagServiceImpl documentRagService;

    /**
     * Test subclass that overrides PDF reader creation to avoid real file I/O.
     */
    static class TestableDocumentRagServiceImpl extends DocumentRagServiceImpl {
        private PagePdfDocumentReader mockReader;

        TestableDocumentRagServiceImpl(VectorStoreRepository repo, TokenTextSplitter splitter) {
            super(repo, splitter);
        }

        void setMockReader(PagePdfDocumentReader reader) {
            this.mockReader = reader;
        }

        @Override
        protected PagePdfDocumentReader createPdfReader(ByteArrayResource resource) {
            return mockReader;
        }
    }

    @BeforeEach
    void setUp() {
        documentRagService = new TestableDocumentRagServiceImpl(vectorStoreRepository, new TokenTextSplitter());
        documentRagService.setMockReader(mockPdfReader);
    }

    @Test
    void testParseSuccess() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "pdf content".getBytes());

        when(mockPdfReader.get()).thenReturn(List.of(
                new Document("page 1 content"),
                new Document("page 2 content"),
                new Document("page 3 content"),
                new Document("page 4 content"),
                new Document("page 5 content")
        ));

        VectorStoreResultDTO storeResult = new VectorStoreResultDTO();
        storeResult.setSuccess(5);
        storeResult.setFail(0);
        when(vectorStoreRepository.store(anyList())).thenReturn(storeResult);

        DocumentRagResultDTO result = documentRagService.parse(file);

        assertNotNull(result);
        assertEquals("test.pdf", result.getFileName());
        assertEquals("success", result.getStatus());
        assertEquals(5, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        verify(vectorStoreRepository).store(anyList());
    }

    @Test
    void testParseWithAllFailures() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "fail.pdf", "application/pdf", "pdf content".getBytes());

        when(mockPdfReader.get()).thenReturn(List.of(
                new Document("content1"),
                new Document("content2"),
                new Document("content3"),
                new Document("content4"),
                new Document("content5")
        ));

        VectorStoreResultDTO storeResult = new VectorStoreResultDTO();
        storeResult.setSuccess(0);
        storeResult.setFail(5);
        when(vectorStoreRepository.store(anyList())).thenReturn(storeResult);

        DocumentRagResultDTO result = documentRagService.parse(file);

        assertNotNull(result);
        assertEquals("failed", result.getStatus());
        assertEquals(0, result.getSuccessCount());
    }

    @Test
    void testParseWithIOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("读取失败"));
        when(file.getOriginalFilename()).thenReturn("broken.pdf");

        DocumentRagResultDTO result = documentRagService.parse(file);

        assertNotNull(result);
        assertEquals("error", result.getStatus());
        assertEquals("broken.pdf", result.getFileName());
    }

    @Test
    void testRetrieve() {
        List<Document> expectedDocs = List.of(
                new Document("检索结果1"),
                new Document("检索结果2")
        );
        when(vectorStoreRepository.retrieval("测试查询", 3, 0.5)).thenReturn(expectedDocs);

        List<Document> results = documentRagService.retrieve("测试查询", 3, 0.5);

        assertEquals(2, results.size());
        verify(vectorStoreRepository).retrieval("测试查询", 3, 0.5);
    }
}
