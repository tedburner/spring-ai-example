package com.ai.knowledge.vector.application.service.impl;

import com.ai.knowledge.vector.application.service.DocumentRagApplicationService;
import com.ai.knowledge.vector.domain.rag.answer.AnswerGenerationDomainService;
import com.ai.knowledge.vector.domain.vector.entity.DocumentRagResultDTO;
import com.ai.knowledge.vector.domain.vector.service.DocumentRagService;
import com.ai.knowledge.vector.domain.vector.service.EmbeddingTextService;
import com.ai.knowledge.vector.interfaces.vo.vector.DocumentRagResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kiturone
 * @date 2025/5/20 19:33
 */
@Service
public class DocumentRagApplicationServiceImpl implements DocumentRagApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentRagApplicationServiceImpl.class);

    private final DocumentRagService documentRagService;
    private final EmbeddingTextService embeddingTextService;
    private final AnswerGenerationDomainService answerGenerationService;

    public DocumentRagApplicationServiceImpl(DocumentRagService documentRagService,
                                             EmbeddingTextService embeddingTextService,
                                             AnswerGenerationDomainService answerGenerationService) {
        this.documentRagService = documentRagService;
        this.embeddingTextService = embeddingTextService;
        this.answerGenerationService = answerGenerationService;
    }

    @Override
    public DocumentRagResultVO parse(MultipartFile file) {
        DocumentRagResultDTO dto = documentRagService.parse(file);
        return toVo(dto);
    }

    /**
     * 检索文档并生成回答（完整 RAG 管道）
     */
    public DocumentRagResultVO ask(String query, int topK, double threshold) {
        List<Document> docs = documentRagService.retrieve(query, topK, threshold);
        String answer = answerGenerationService.generateAnswer(query, docs);

        DocumentRagResultVO vo = new DocumentRagResultVO();
        vo.setDocNum(docs.size());
        vo.setAnswer(answer);
        vo.setRetrievalResults(docs.stream().map(doc -> {
            DocumentRagResultVO.RetrievalResult result = new DocumentRagResultVO.RetrievalResult();
            result.setContent(doc.getText());
            result.setMetadata(doc.getMetadata());
            return result;
        }).collect(Collectors.toList()));
        return vo;
    }

    private DocumentRagResultVO toVo(DocumentRagResultDTO dto) {
        DocumentRagResultVO vo = new DocumentRagResultVO();
        vo.setDocNum(dto.getDocNum());
        vo.setStatus(dto.getStatus());
        vo.setFileName(dto.getFileName());
        vo.setChunksCount(dto.getChunksCount());
        vo.setSuccessCount(dto.getSuccessCount());
        vo.setFailCount(dto.getFailCount());
        return vo;
    }
}
