package com.ai.chat.application.service.dto;

import java.util.List;

/**
 * Chain 工作流结果：记录每步的输入输出及最终结果。
 */
public record ChainResult(
        String initialPrompt,
        List<ChainStep> steps,
        String finalOutput
) {
    public record ChainStep(
            int stepNumber,
            String prompt,
            String output
    ) {}
}
