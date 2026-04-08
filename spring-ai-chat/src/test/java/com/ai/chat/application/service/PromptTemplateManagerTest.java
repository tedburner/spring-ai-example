package com.ai.chat.application.service;

import com.ai.chat.application.config.PromptTemplateProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: kiturone
 * @date: 2026/4/4
 * @description: PromptTemplateManager 单元测试
 */
class PromptTemplateManagerTest {

    private PromptTemplateManager promptTemplateManager;
    private PromptTemplateProperties properties;

    @BeforeEach
    void setUp() {
        properties = new PromptTemplateProperties();
        properties.setDefaultRole("AI助手");
        Map<String, String> customTemplates = new HashMap<>();
        customTemplates.put("test-template", "这是测试模板：{content}");
        properties.setTemplates(customTemplates);

        promptTemplateManager = new PromptTemplateManager(properties);
    }

    @Test
    void testGeneratePromptWithExistingTemplate() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("role", "数据分析师");
        variables.put("content", "分析这段数据");

        String prompt = promptTemplateManager.generatePrompt("analytical", variables);

        assertNotNull(prompt);
        assertTrue(prompt.contains("数据分析师"));
        assertTrue(prompt.contains("分析这段数据"));
    }

    @Test
    void testGeneratePromptWithCustomTemplate() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("content", "测试内容");

        String prompt = promptTemplateManager.generatePrompt("test-template", variables);

        assertEquals("这是测试模板：测试内容", prompt);
    }

    @Test
    void testGeneratePromptWithNonExistentTemplate() {
        Map<String, Object> variables = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> {
            promptTemplateManager.generatePrompt("non-existent", variables);
        });
    }

    @Test
    void testGeneratePromptWithDefaultRole() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("content", "测试内容");

        String prompt = promptTemplateManager.generatePrompt("analytical", variables);

        assertTrue(prompt.contains("AI助手"));
    }

    @Test
    void testAddCustomTemplate() {
        String templateName = "custom-test";
        String templateContent = "自定义模板：{param}";

        promptTemplateManager.addTemplate(templateName, templateContent);

        assertTrue(promptTemplateManager.hasTemplate(templateName));

        Map<String, Object> variables = new HashMap<>();
        variables.put("param", "测试参数");
        String prompt = promptTemplateManager.generatePrompt(templateName, variables);

        assertEquals("自定义模板：测试参数", prompt);
    }

    @Test
    void testGetTemplateNames() {
        var templateNames = promptTemplateManager.getTemplateNames();

        assertNotNull(templateNames);
        assertTrue(templateNames.contains("analytical"));
        assertTrue(templateNames.contains("summarization"));
        assertTrue(templateNames.contains("translation"));
        assertTrue(templateNames.contains("test-template"));
    }

    @Test
    void testHasTemplate() {
        assertTrue(promptTemplateManager.hasTemplate("analytical"));
        assertFalse(promptTemplateManager.hasTemplate("non-existent"));
    }

    @Test
    void testTranslationTemplate() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("targetLanguage", "英语");
        variables.put("content", "你好世界");

        String prompt = promptTemplateManager.generatePrompt("translation", variables);

        assertTrue(prompt.contains("英语"));
        assertTrue(prompt.contains("你好世界"));
    }
}