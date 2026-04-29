package com.ai.chat.application.service.dto;

import java.util.List;

/**
 * Parallel 工作流结果：支持 Sectioning（分节）和 Voting（投票）两种模式。
 */
public record ParallelResult(
        String mode,
        String mainPrompt,
        List<ParallelStep> steps,
        String consensus
) {
    public record ParallelStep(
            String sectionPrompt,
            String output
    ) {}
}
