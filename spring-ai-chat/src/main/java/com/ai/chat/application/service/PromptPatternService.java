package com.ai.chat.application.service;

import com.ai.chat.application.config.PromptPatternOptions;
import com.ai.chat.application.config.PromptPatternOptions.PatternConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author: kiturone
 * @date: 2026/04/28
 * @description: Prompt Engineering Pattern 核心服务，提供 9 种基础 Pattern 和配置化管理
 */
@Service
public class PromptPatternService {

    private final ChatClient chatClient;
    private final PromptPatternOptions patternOptions;

    public PromptPatternService(ChatModel chatModel, PromptPatternOptions patternOptions) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.patternOptions = patternOptions;
        initializeDefaultPresets();
    }

    private void initializeDefaultPresets() {
        Map<String, PatternConfig> presets = patternOptions.getPresets();
        presets.putIfAbsent("zero-shot", new PatternConfig(0.1, 0, 5));
        presets.putIfAbsent("few-shot", new PatternConfig(0.1, 0, 250));
        presets.putIfAbsent("creative", new PatternConfig(1.0, 40, 1024));
        presets.putIfAbsent("code", new PatternConfig(0.1, 0, 1024));
        presets.putIfAbsent("reasoning", new PatternConfig(0.7, 0, 500));
        presets.putIfAbsent("self-consistency", new PatternConfig(1.0, 60, 1024, 5));
    }

    private ChatOptions buildOptions(String presetName) {
        PatternConfig config = patternOptions.get(presetName);
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
     * Zero-shot Prompting
     * 不提供示例，直接让模型完成任务。适用于分类、情感判断等。
     */
    public String zeroShot(String prompt) {
        return chatClient.prompt(prompt)
                .options(buildOptions("zero-shot"))
                .call()
                .content();
    }

    /**
     * Zero-shot Prompting + 结构化输出
     */
    public <T> T zeroShotEntity(String prompt, Class<T> responseType) {
        return chatClient.prompt(prompt)
                .options(buildOptions("zero-shot"))
                .call()
                .entity(responseType);
    }

    /**
     * One-shot / Few-shot Prompting
     * 在 prompt 中内嵌示例，引导模型遵循特定格式。
     */
    public String fewShot(String prompt) {
        return chatClient.prompt(prompt)
                .options(buildOptions("few-shot"))
                .call()
                .content();
    }

    /**
     * System Prompting
     * 使用 .system() 明确定义角色和输出格式约束。
     */
    public String systemPrompt(String system, String user) {
        return chatClient.prompt()
                .system(system)
                .user(user)
                .options(buildOptions("creative"))
                .call()
                .content();
    }

    /**
     * System Prompting + 结构化输出
     */
    public <T> T systemEntity(String system, String user, Class<T> responseType) {
        return chatClient.prompt()
                .system(system)
                .user(user)
                .options(buildOptions("creative"))
                .call()
                .entity(responseType);
    }

    /**
     * Role Prompting
     * system 消息中定义角色（"I want you to act as..."）。
     */
    public String rolePrompt(String role, String user) {
        return chatClient.prompt()
                .system("I want you to act as " + role)
                .user(user)
                .options(buildOptions("creative"))
                .call()
                .content();
    }

    /**
     * Contextual Prompting
     * 手动替换模板变量，动态注入上下文参数。
     */
    public String contextualPrompt(String template, Map<String, Object> params) {
        String resolved = replaceVariables(template, params);
        return chatClient.prompt(resolved)
                .options(buildOptions("creative"))
                .call()
                .content();
    }

    /**
     * Chain of Thought (CoT)
     * 在问题末尾追加 "Let's think step by step." 触发逐步推理。
     */
    public String chainOfThought(String question) {
        String cotPrompt = question + "\n\nLet's think step by step.";
        return chatClient.prompt(cotPrompt)
                .options(buildOptions("reasoning"))
                .call()
                .content();
    }

    /**
     * Chain of Thought + Few-shot
     * 提供一个完整推理示例，再提出问题。
     */
    public String chainOfThoughtFewShot(String example, String question) {
        String prompt = example + "\n\nQ: " + question + "\nLet's think step by step.\nA:";
        return chatClient.prompt(prompt)
                .options(buildOptions("reasoning"))
                .call()
                .content();
    }

    /**
     * Code Writing
     * 根据描述生成代码，使用低温度保证输出确定性。
     */
    public String codeWriting(String description, String language) {
        String prompt = "Write a code snippet in " + language + " that does the following:\n" + description;
        return chatClient.prompt(prompt)
                .options(buildOptions("code"))
                .call()
                .content();
    }

    /**
     * Code Explaining
     * 解释给定代码的功能和逻辑。
     */
    public String codeExplaining(String code) {
        String prompt = "Explain the below code and describe what it does:\n```\n" + code + "\n```";
        return chatClient.prompt(prompt)
                .options(buildOptions("code"))
                .call()
                .content();
    }

    /**
     * Code Translating
     * 将代码从一种语言翻译到另一种语言。
     */
    public String codeTranslating(String code, String targetLanguage) {
        String prompt = "Translate the below code to " + targetLanguage + ":\n```\n" + code + "\n```";
        return chatClient.prompt(prompt)
                .options(buildOptions("code"))
                .call()
                .content();
    }

    private String replaceVariables(String template, Map<String, Object> params) {
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
