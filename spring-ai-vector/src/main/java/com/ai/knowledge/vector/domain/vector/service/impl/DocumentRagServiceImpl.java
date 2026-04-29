package com.ai.knowledge.vector.domain.vector.service.impl;

import com.ai.knowledge.vector.domain.vector.entity.DocumentRagResultDTO;
import com.ai.knowledge.vector.domain.vector.entity.VectorStoreResultDTO;
import com.ai.knowledge.vector.domain.vector.repository.VectorStoreRepository;
import com.ai.knowledge.vector.domain.vector.service.DocumentRagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author kiturone
 * @date 2025/5/20 19:35
 */
@Service
public class DocumentRagServiceImpl implements DocumentRagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentRagServiceImpl.class);

    private final VectorStoreRepository vectorStoreRepository;
    private final TokenTextSplitter textSplitter;

    public DocumentRagServiceImpl(VectorStoreRepository vectorStoreRepository) {
        this(vectorStoreRepository, null);
    }

    DocumentRagServiceImpl(VectorStoreRepository vectorStoreRepository, TokenTextSplitter textSplitter) {
        this.vectorStoreRepository = vectorStoreRepository;
        this.textSplitter = textSplitter != null ? textSplitter : new TokenTextSplitter();
    }

    protected PagePdfDocumentReader createPdfReader(ByteArrayResource resource) {
        return new PagePdfDocumentReader(resource);
    }

    @Override
    public DocumentRagResultDTO parse(MultipartFile file) {
        DocumentRagResultDTO result = new DocumentRagResultDTO();
        try {
            byte[] pdfBytes = file.getBytes();
            PagePdfDocumentReader reader = createPdfReader(new ByteArrayResource(pdfBytes));
            List<Document> documents = reader.get();
            LOGGER.info("PDF 解析完成，共 {} 页", documents.size());

            List<Document> chunks = textSplitter.apply(documents);
            LOGGER.info("文档分块完成，共 {} 个块", chunks.size());

            VectorStoreResultDTO storeResult = vectorStoreRepository.store(chunks);
            LOGGER.info("向量存储完成，成功: {}, 失败: {}", storeResult.getSuccess(), storeResult.getFail());

            result.setDocNum(chunks.size());
            result.setFileName(file.getOriginalFilename());
            result.setChunksCount(chunks.size());
            result.setSuccessCount(storeResult.getSuccess());
            result.setFailCount(storeResult.getFail());
            result.setStatus(storeResult.getSuccess() > 0 ? "success" : "failed");

        } catch (Exception e) {
            LOGGER.error("文档解析失败", e);
            result.setStatus("error");
            result.setFileName(file.getOriginalFilename());
        }
        return result;
    }

    @Override
    public List<Document> retrieve(String query, int topK, double threshold) {
        return vectorStoreRepository.retrieval(query, topK, threshold);
    }
}
