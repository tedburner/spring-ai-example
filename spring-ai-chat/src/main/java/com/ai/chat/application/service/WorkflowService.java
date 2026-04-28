package com.ai.chat.application.service;

import com.ai.chat.application.config.PromptPatternOptions;
import com.ai.chat.application.config.PromptPatternOptions.PatternConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: kiturone
 * @date: 2026/04/28
 * @description: 复合 Prompt 工作流编排服务，实现 Step-Back、Self-Consistency、Tree of Thoughts、Auto Prompt Engineering
 */
@Service
public class WorkflowService {

    private final ChatClient chatClient;
    private final PromptPatternService patternService;
    private final PromptPatternOptions patternOptions;

    public WorkflowService(ChatModel chatModel, PromptPatternService patternService, PromptPatternOptions patternOptions) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.patternService = patternService;
        this.patternOptions = patternOptions;
    }

    /**
     * Step-Back Prompting
     * 1. 先针对问题的更通用版本提问（"退一步"）
     * 2. 将通用回答作为上下文，回答原始具体问题
     */
    public String stepBack(String specificQuestion) {
        PatternConfig config = patternOptions.get("creative");

        // Step 1: 生成更通用的"退一步"问题
        String stepBackPrompt = "基于以下具体问题，先提出一个更通用、更基础的问题：\n问题：" + specificQuestion;
        String generalQuestion = chatClient.prompt(stepBackPrompt)
                .options(buildOptions(config))
                .call()
                .content();

        // Step 2: 回答通用问题
        String generalAnswer = chatClient.prompt(generalQuestion)
                .options(buildOptions(config))
                .call()
                .content();

        // Step 3: 将通用答案作为上下文，回答原始具体问题
        String step3Template = """
                基于以下背景知识，请回答具体问题：
                背景：{context}
                问题：{question}
                """.replace("{context}", generalAnswer).replace("{question}", specificQuestion);
        return chatClient.prompt(step3Template)
                .options(buildOptions(config))
                .call()
                .content();
    }

    /**
     * Self-Consistency
     * 以较高温度多次调用同一 prompt，投票取多数答案。
     *
     * @param prompt       原始提示词
     * @param votes        投票次数
     * @param classificationPrompt 用于将结果转为可投票分类的 prompt（可为 null）
     * @return 得票最多的结果
     */
    public String selfConsistency(String prompt, int votes, String classificationPrompt) {
        PatternConfig config = patternOptions.get("self-consistency");
        Map<String, Integer> voteCounts = new HashMap<>();

        for (int i = 0; i < votes; i++) {
            String fullPrompt = (classificationPrompt != null ? classificationPrompt + "\n\n" : "") + prompt;
            String result = chatClient.prompt(fullPrompt)
                    .options(buildOptions(config))
                    .call()
                    .content();
            voteCounts.merge(result.trim(), 1, Integer::sum);
        }

        return voteCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (得票: " + e.getValue() + "/" + votes + ")")
                .orElse("无结果");
    }

    /**
     * Tree of Thoughts (ToT)
     * 1. 生成 N 种思路
     * 2. 评估选择最佳
     * 3. 基于最佳思路细化
     */
    public String treeOfThoughts(String problem, int branches) {
        PatternConfig config = patternOptions.get("reasoning");

        // Step 1: 生成 N 种不同的思路
        String approaches = chatClient.prompt(
                "针对以下问题，生成 " + branches + " 种不同的解决方案，每种方案用编号分隔：\n问题：" + problem)
                .options(buildOptions(config))
                .call()
                .content();

        // Step 2: 评估并选择最佳思路
        String step2Template = """
                以下是对问题的 {count} 种解决方案：
                {approaches}
                请分析各方案的优劣，并选择最佳方案，说明理由。
                """.replace("{count}", String.valueOf(branches)).replace("{approaches}", approaches);
        String bestApproach = chatClient.prompt(step2Template)
                .options(buildOptions(config))
                .call()
                .content();

        // Step 3: 基于最佳思路细化
        String step3Template = """
                基于以下分析：
                {analysis}
                请给出最终的完整解决方案。
                """.replace("{analysis}", bestApproach);
        return chatClient.prompt(step3Template)
                .options(buildOptions(config))
                .call()
                .content();
    }

    /**
     * Automatic Prompt Engineering
     * 1. 生成 N 个 prompt 变体
     * 2. LLM 评估各变体的质量，选最佳
     */
    public String autoPromptEngineering(String task, int variants) {
        PatternConfig config = patternOptions.get("creative");

        // Step 1: 生成多个 prompt 变体
        String variantsPrompt = "为以下任务生成 " + variants + " 个不同的 prompt 变体，每个用 === 分隔：\n任务：" + task;
        String variantsOutput = chatClient.prompt(variantsPrompt)
                .options(buildOptions(config))
                .call()
                .content();

        // Step 2: LLM 评估并选最佳
        String step2Template = """
                以下是对同一任务的 {count} 个 prompt 变体：
                {variants}
                请评估各变体的质量（清晰度、具体性、可执行性），选出最佳的一个，并说明理由。
                """.replace("{count}", String.valueOf(variants)).replace("{variants}", variantsOutput);
        return chatClient.prompt(step2Template)
                .options(buildOptions(config))
                .call()
                .content();
    }

    private ChatOptions buildOptions(PatternConfig config) {
        if (config == null) {
            return ChatOptions.builder().build();
        }
        var builder = ChatOptions.builder()
                .temperature(config.temperature())
                .maxTokens(config.maxTokens());
        if (config.topK() > 0) {
            builder.topK(config.topK());
        }
        return builder.build();
    }
}
