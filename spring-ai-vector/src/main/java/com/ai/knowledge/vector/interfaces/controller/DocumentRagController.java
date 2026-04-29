package com.ai.knowledge.vector.interfaces.controller;

import com.ai.common.http.WebResult;
import com.ai.knowledge.vector.application.service.impl.DocumentRagApplicationServiceImpl;
import com.ai.knowledge.vector.interfaces.vo.vector.DocumentRagResultVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档 RAG
 */
@RestController
@RequestMapping("/document/rag")
public class DocumentRagController {

    private final DocumentRagApplicationServiceImpl documentRagApplicationService;

    public DocumentRagController(DocumentRagApplicationServiceImpl documentRagApplicationService) {
        this.documentRagApplicationService = documentRagApplicationService;
    }

    /**
     * 上传文档并解析
     */
    @PostMapping("/v1/parse")
    public WebResult uploadParse(@RequestParam("file") MultipartFile file) {
        final DocumentRagResultVO data = documentRagApplicationService.parse(file);
        return WebResult.buildSuccess(data);
    }

    /**
     * 检索文档并生成回答（完整 RAG 管道）
     */
    @PostMapping("/v1/ask")
    public WebResult askQuestion(
            @RequestParam("query") String query,
            @RequestParam(value = "topK", defaultValue = "5") Integer topK,
            @RequestParam(value = "threshold", defaultValue = "0.5") Double threshold) {
        DocumentRagResultVO data = documentRagApplicationService.ask(query, topK, threshold);
        return WebResult.buildSuccess(data);
    }
}
