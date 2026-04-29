package com.ai.knowledge.vector.domain.vector.entity;

import lombok.Data;

/**
 * 文档 RAG 结果 DTO
 */
@Data
public class DocumentRagResultDTO {

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
}
