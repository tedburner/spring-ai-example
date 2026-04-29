package com.ai.chat.application.service;

import com.ai.chat.application.config.PromptPatternOptions;
import com.ai.chat.application.config.PromptPatternOptions.PatternConfig;
import com.ai.chat.application.service.dto.ChainResult;
import com.ai.chat.application.service.dto.ParallelResult;
import com.ai.chat.application.service.dto.RouteClassification;
import com.ai.chat.application.service.dto.RoutingDecision;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    /**
     * Chain Workflow — 顺序 LLM 调用管道，每步输出作为下一步输入。
     * 适合数据清洗管道、多步内容生成等场景。
     */
    public ChainResult chainWorkflow(String initialPrompt, List<String> stepPrompts) {
        List<ChainResult.ChainStep> steps = new ArrayList<>();
        String currentInput = initialPrompt;

        for (int i = 0; i < stepPrompts.size(); i++) {
            String stepPrompt = stepPrompts.get(i).replace("{input}", currentInput);
            String output = chatClient.prompt(stepPrompt)
                    .options(buildOptions(patternOptions.get("creative")))
                    .call()
                    .content();
            steps.add(new ChainResult.ChainStep(i + 1, stepPrompts.get(i), output));
            currentInput = output;
        }

        return new ChainResult(initialPrompt, steps, currentInput);
    }

    /**
     * Parallel Sectioning — 将主任务拆分为独立子任务并发执行。
     */
    public ParallelResult parallelSectioning(String mainPrompt, List<String> sectionPrompts) {
        if (sectionPrompts.isEmpty()) {
            return new ParallelResult("sectioning", mainPrompt, List.of(), "");
        }

        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(sectionPrompts.size(), Runtime.getRuntime().availableProcessors()));

        List<CompletableFuture<ParallelResult.ParallelStep>> futures = sectionPrompts.stream()
                .map(section -> CompletableFuture.supplyAsync(() -> {
                    String prompt = mainPrompt + "\n\n子任务: " + section;
                    String output = chatClient.prompt(prompt)
                            .options(buildOptions(patternOptions.get("creative")))
                            .call()
                            .content();
                    return new ParallelResult.ParallelStep(section, output);
                }, executor))
                .toList();

        List<ParallelResult.ParallelStep> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        executor.shutdown();

        String combined = results.stream()
                .map(r -> "## " + r.sectionPrompt() + "\n" + r.output())
                .collect(Collectors.joining("\n\n---\n\n"));

        return new ParallelResult("sectioning", mainPrompt, results, combined);
    }

    /**
     * Parallel Voting — 同一 prompt 多次调用，通过精确匹配统计多数共识。
     */
    public ParallelResult parallelVoting(String prompt, int votes) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(votes, Runtime.getRuntime().availableProcessors()));

        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < votes; i++) {
            futures.add(CompletableFuture.supplyAsync(() ->
                    chatClient.prompt(prompt)
                            .options(buildOptions(patternOptions.get("self-consistency")))
                            .call()
                            .content(), executor));
        }

        List<String> outputs = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        executor.shutdown();

        Map<String, Integer> voteCounts = new HashMap<>();
        for (String output : outputs) {
            voteCounts.merge(output.trim(), 1, Integer::sum);
        }

        String consensus = voteCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (得票: " + e.getValue() + "/" + votes + ")")
                .orElse("无结果");

        List<ParallelResult.ParallelStep> steps = outputs.stream()
                .map(o -> new ParallelResult.ParallelStep(prompt, o))
                .toList();

        return new ParallelResult("voting", prompt, steps, consensus);
    }

    /**
     * Routing Workflow — 用结构化输出将输入分类到不同路由，每个路由对应 specialized prompt。
     */
    public RoutingDecision routingWorkflow(String input, Map<String, String> routePrompts) {
        String routes = String.join(", ", routePrompts.keySet());

        String classificationPrompt = """
                将以下输入分类到以下类别之一: %s。
                以 JSON 格式返回你的推理过程和选中的类别。
                输入: %s
                """.formatted(routes, input);

        BeanOutputConverter<RouteClassification> converter = new BeanOutputConverter<>(RouteClassification.class);
        String classificationJson = chatClient.prompt(classificationPrompt)
                .options(buildOptions(patternOptions.get("reasoning")))
                .call()
                .content();

        RouteClassification classification = converter.convert(classificationJson);
        if (classification == null) {
            classification = new RouteClassification("无法分类", routePrompts.keySet().iterator().next());
        }

        String selectedRoute = classification.selection();
        String specializedPrompt = routePrompts.getOrDefault(selectedRoute, "请直接回答: {input}");
        String result = chatClient.prompt(specializedPrompt.replace("{input}", input))
                .options(buildOptions(patternOptions.get("creative")))
                .call()
                .content();

        return new RoutingDecision(input, classification, result);
    }
}
