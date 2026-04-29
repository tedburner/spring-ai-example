package com.ai.knowledge.vector.interfaces.vo.vector;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 文档 RAG 结果 VO
 */
@Data
public class DocumentRagResultVO {

    /** 文档数据条数 */
    private Integer docNum;

    /** 处理状态: success / failed / error */
    private String status;

    /** 原始文件名 */
    private String fileName;

    /** 分块总数 */
    private Integer chunksCount;

    /** 存储成功数 */
    private Integer successCount;

    /** 存储失败数 */
    private Integer failCount;

    /** 检索结果列表（仅 ask 端点返回） */
    private List<RetrievalResult> retrievalResults;

    /** AI 生成的回答（仅 ask 端点返回） */
    private String answer;

    @Data
    public static class RetrievalResult {
        private String content;
        private Double similarityScore;
        private Map<String, Object> metadata;
    }
}
