package com.ai.chat.interfaces.controller;

import com.ai.chat.application.service.PromptPatternService;
import com.ai.chat.application.service.WorkflowService;
import com.ai.chat.interfaces.dto.PromptPatternRequest;
import com.ai.chat.interfaces.dto.PromptPatternResponse;
import com.ai.common.http.WebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author: kiturone
 * @date: 2026/04/28
 * @description: Prompt Engineering Pattern REST 控制器
 */
@RestController
@RequestMapping("/pattern")
public class PromptPatternController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptPatternController.class);

    private final PromptPatternService patternService;
    private final WorkflowService workflowService;

    public PromptPatternController(PromptPatternService patternService, WorkflowService workflowService) {
        this.patternService = patternService;
        this.workflowService = workflowService;
    }

    /**
     * 统一 Pattern 调用端点
     */
    @PostMapping("/invoke")
    public WebResult invoke(@RequestBody PromptPatternRequest request) {
        try {
            LOGGER.info("Pattern 调用：type={}", request.getType());
            String result = dispatch(request);
            return WebResult.buildSuccess(new PromptPatternResponse(request.getType(), result, request.getType()));
        } catch (Exception e) {
            LOGGER.error("Pattern 调用失败", e);
            return WebResult.buildFail("Pattern 调用失败: " + e.getMessage());
        }
    }

    /**
     * Zero-shot Prompting
     */
    @PostMapping("/zero-shot")
    public WebResult zeroShot(@RequestBody PromptPatternRequest request) {
        LOGGER.info("Zero-shot: {}", request.getPrompt());
        String result = patternService.zeroShot(request.getPrompt());
        return WebResult.buildSuccess(new PromptPatternResponse("zero-shot", result, "zero-shot"));
    }

    /**
     * Few-shot Prompting
     */
    @PostMapping("/few-shot")
    public WebResult fewShot(@RequestBody PromptPatternRequest request) {
        String prompt = buildFewShotPrompt(request.getPrompt(), request.getExamples());
        LOGGER.info("Few-shot: {}", prompt);
        String result = patternService.fewShot(prompt);
        return WebResult.buildSuccess(new PromptPatternResponse("few-shot", result, "few-shot"));
    }

    /**
     * System Prompting
     */
    @PostMapping("/system")
    public WebResult system(@RequestBody PromptPatternRequest request) {
        LOGGER.info("System: {}", request.getSystem());
        String result = patternService.systemPrompt(request.getSystem(), request.getPrompt());
        return WebResult.buildSuccess(new PromptPatternResponse("system", result, "creative"));
    }

    /**
     * Role Prompting
     */
    @PostMapping("/role")
    public WebResult role(@RequestBody PromptPatternRequest request) {
        LOGGER.info("Role: {}", request.getRole());
        String result = patternService.rolePrompt(request.getRole(), request.getPrompt());
        return WebResult.buildSuccess(new PromptPatternResponse("role", result, "creative"));
    }

    /**
     * Contextual Prompting
     */
    @PostMapping("/contextual")
    public WebResult contextual(@RequestBody PromptPatternRequest request) {
        LOGGER.info("Contextual: variables={}", request.getVariables());
        String result = patternService.contextualPrompt(request.getPrompt(), request.getVariables());
        return WebResult.buildSuccess(new PromptPatternResponse("contextual", result, "creative"));
    }

    /**
     * Chain of Thought
     */
    @PostMapping("/cot")
    public WebResult chainOfThought(@RequestBody PromptPatternRequest request) {
        LOGGER.info("CoT: {}", request.getPrompt());
        String result;
        if (request.getCotExample() != null && !request.getCotExample().isBlank()) {
            result = patternService.chainOfThoughtFewShot(request.getCotExample(), request.getPrompt());
        } else {
            result = patternService.chainOfThought(request.getPrompt());
        }
        return WebResult.buildSuccess(new PromptPatternResponse("cot", result, "reasoning"));
    }

    /**
     * Code Writing
     */
    @PostMapping("/code/write")
    public WebResult codeWrite(@RequestBody PromptPatternRequest request) {
        String language = defaultIfNull(request.getTargetLanguage(), "Java");
        LOGGER.info("Code Write: language={}", language);
        String result = patternService.codeWriting(request.getPrompt(), language);
        return WebResult.buildSuccess(new PromptPatternResponse("code-write", result, "code"));
    }

    /**
     * Code Explaining
     */
    @PostMapping("/code/explain")
    public WebResult codeExplain(@RequestBody PromptPatternRequest request) {
        LOGGER.info("Code Explain");
        String result = patternService.codeExplaining(request.getCode());
        return WebResult.buildSuccess(new PromptPatternResponse("code-explain", result, "code"));
    }

    /**
     * Code Translating
     */
    @PostMapping("/code/translate")
    public WebResult codeTranslate(@RequestBody PromptPatternRequest request) {
        String targetLanguage = defaultIfNull(request.getTargetLanguage(), "Python");
        LOGGER.info("Code Translate: to={}", targetLanguage);
        String result = patternService.codeTranslating(request.getCode(), targetLanguage);
        return WebResult.buildSuccess(new PromptPatternResponse("code-translate", result, "code"));
    }

    /**
     * Step-Back Prompting
     */
    @PostMapping("/step-back")
    public WebResult stepBack(@RequestBody PromptPatternRequest request) {
        LOGGER.info("Step-Back: {}", request.getPrompt());
        String result = workflowService.stepBack(request.getPrompt());
        return WebResult.buildSuccess(new PromptPatternResponse("step-back", result, "creative"));
    }

    /**
     * Self-Consistency
     */
    @PostMapping("/self-consistency")
    public WebResult selfConsistency(@RequestBody PromptPatternRequest request) {
        int votes = defaultIfNull(request.getCount(), 5);
        LOGGER.info("Self-Consistency: votes={}", votes);
        String result = workflowService.selfConsistency(request.getPrompt(), votes, null);
        return WebResult.buildSuccess(new PromptPatternResponse("self-consistency", result, "self-consistency"));
    }

    /**
     * Tree of Thoughts
     */
    @PostMapping("/tree-of-thoughts")
    public WebResult treeOfThoughts(@RequestBody PromptPatternRequest request) {
        int branches = defaultIfNull(request.getCount(), 3);
        LOGGER.info("Tree of Thoughts: branches={}", branches);
        String result = workflowService.treeOfThoughts(request.getPrompt(), branches);
        return WebResult.buildSuccess(new PromptPatternResponse("tree-of-thoughts", result, "reasoning"));
    }

    /**
     * Automatic Prompt Engineering
     */
    @PostMapping("/auto-prompt")
    public WebResult autoPrompt(@RequestBody PromptPatternRequest request) {
        int variants = defaultIfNull(request.getCount(), 5);
        LOGGER.info("Auto Prompt: variants={}", variants);
        String result = workflowService.autoPromptEngineering(request.getPrompt(), variants);
        return WebResult.buildSuccess(new PromptPatternResponse("auto-prompt", result, "creative"));
    }

    /**
     * 获取所有可用的 Pattern 名称
     */
    @GetMapping("/types")
    public WebResult getPatternTypes() {
        return WebResult.buildSuccess(List.of(
                "zero-shot", "few-shot", "system", "role", "contextual",
                "cot", "code-write", "code-explain", "code-translate",
                "step-back", "self-consistency", "tree-of-thoughts", "auto-prompt"
        ));
    }

    private String buildFewShotPrompt(String prompt, List<PromptPatternRequest.Example> examples) {
        if (examples == null || examples.isEmpty()) {
            return prompt;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < examples.size(); i++) {
            var ex = examples.get(i);
            sb.append("EXAMPLE ").append(i + 1).append(": ").append(ex.getInput()).append("\n");
            sb.append("JSON Response: ").append(ex.getOutput()).append("\n\n");
        }
        sb.append("Now: ").append(prompt);
        return sb.toString();
    }

    private String dispatch(PromptPatternRequest request) {
        return switch (request.getType()) {
            case "zero-shot" -> patternService.zeroShot(request.getPrompt());
            case "few-shot" -> patternService.fewShot(buildFewShotPrompt(request.getPrompt(), request.getExamples()));
            case "system" -> patternService.systemPrompt(request.getSystem(), request.getPrompt());
            case "role" -> patternService.rolePrompt(request.getRole(), request.getPrompt());
            case "contextual" -> patternService.contextualPrompt(request.getPrompt(), request.getVariables());
            case "cot" -> request.getCotExample() != null && !request.getCotExample().isBlank()
                    ? patternService.chainOfThoughtFewShot(request.getCotExample(), request.getPrompt())
                    : patternService.chainOfThought(request.getPrompt());
            case "code-write" -> patternService.codeWriting(request.getPrompt(), defaultIfNull(request.getTargetLanguage(), "Java"));
            case "code-explain" -> patternService.codeExplaining(request.getCode());
            case "code-translate" -> patternService.codeTranslating(request.getCode(), defaultIfNull(request.getTargetLanguage(), "Python"));
            case "step-back" -> workflowService.stepBack(request.getPrompt());
            case "self-consistency" -> workflowService.selfConsistency(request.getPrompt(), defaultIfNull(request.getCount(), 5), null);
            case "tree-of-thoughts" -> workflowService.treeOfThoughts(request.getPrompt(), defaultIfNull(request.getCount(), 3));
            case "auto-prompt" -> workflowService.autoPromptEngineering(request.getPrompt(), defaultIfNull(request.getCount(), 5));
            default -> throw new IllegalArgumentException("Unknown pattern type: " + request.getType());
        };
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
