package com.ai.chat.application.service;

import com.ai.chat.application.config.PromptTemplateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 提示词模板管理服务
 *
 * @author kiturone
 * @date 2026/4/4
 */
@Service
public class PromptTemplateManager {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateManager.class);

    private final Map<String, String> templates;
    private final String defaultRole;

    public PromptTemplateManager(PromptTemplateProperties properties) {
        this.templates = new HashMap<>(properties.getTemplates());
        this.defaultRole = properties.getDefaultRole();
        initializeDefaultTemplates();
        logger.info("已加载 {} 个提示词模板", templates.size());
    }

    /**
     * 初始化默认模板
     */
    private void initializeDefaultTemplates() {
        templates.putIfAbsent("analytical",
            "你是{role}，请对以下内容进行深入分析，给出专业见解：{content}");

        templates.putIfAbsent("summarization",
            "请对以下内容进行简洁总结，突出重点：{content}");

        templates.putIfAbsent("translation",
            "请将以下文本翻译为{targetLanguage}，保持原文的语气和风格：{content}");

        templates.putIfAbsent("creative-writing",
            "请基于主题'{theme}'创作一段{length}字左右的{genre}内容：{content}");

        templates.putIfAbsent("code-review",
            "你是一位经验丰富的代码审查专家。请审查以下代码，指出潜在问题并提供改进建议：{content}");

        templates.putIfAbsent("qa-assistant",
            "你是一个专业的问答助手。请基于以下问题提供准确、详细的答案：{content}");
    }

    /**
     * 生成提示词
     *
     * @param templateName 模板名称
     * @param variables 变量映射
     * @return 生成的提示词
     */
    public String generatePrompt(String templateName, Map<String, Object> variables) {
        String template = templates.get(templateName);
        if (template == null) {
            logger.warn("未找到模板: {}, 使用默认模板", templateName);
            throw new IllegalArgumentException("未找到提示词模板: " + templateName);
        }

        String prompt = replaceVariables(template, variables);
        logger.debug("生成提示词，模板: {}, 变量: {}", templateName, variables.keySet());
        return prompt;
    }

    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        String result = template;

        // 替换用户提供的变量
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }

        // 替换默认角色
        if (result.contains("{role}") && !variables.containsKey("role")) {
            result = result.replace("{role}", defaultRole);
        }

        return result;
    }

    /**
     * 添加自定义模板
     */
    public void addTemplate(String name, String template) {
        templates.put(name, template);
        logger.info("添加自定义模板: {}", name);
    }

    /**
     * 获取所有模板名称
     */
    public java.util.Set<String> getTemplateNames() {
        return templates.keySet();
    }

    /**
     * 检查模板是否存在
     */
    public boolean hasTemplate(String templateName) {
        return templates.containsKey(templateName);
    }
}