package com.ai.knowledge.vector.domain.vector.service;

import com.ai.knowledge.vector.domain.vector.entity.DocumentRagResultDTO;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档 RAG 领域服务 — 负责 PDF 解析、分块、存储与检索。
 */
public interface DocumentRagService {

    /**
     * 解析 PDF 文件，分块、嵌入、存储
     */
    DocumentRagResultDTO parse(MultipartFile file);

    /**
     * 检索与查询语义相近的文档
     */
    List<Document> retrieve(String query, int topK, double threshold);
}
