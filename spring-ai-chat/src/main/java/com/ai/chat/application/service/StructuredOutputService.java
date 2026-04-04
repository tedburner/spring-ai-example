package com.ai.chat.application.service;

import com.ai.chat.domain.entity.TextAnalysis;
import com.ai.chat.domain.entity.WeatherData;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StructuredOutputService {

    private static final Logger logger = LoggerFactory.getLogger(StructuredOutputService.class);

    @Autowired(required = false)
    private ChatModel chatModel;

    public <T> T convertToStructure(String prompt, Class<T> targetClass) {
        try {
            logger.debug("开始转换结构化输出，目标类型: {}", targetClass.getSimpleName());

            BeanOutputConverter<T> outputConverter = new BeanOutputConverter<>(targetClass);
            String formatInstructions = outputConverter.getFormat();

            // 构建带有格式要求的提示词
            String fullPrompt = String.format(
                "%s\n\n%s",
                prompt,
                formatInstructions
            );

            logger.debug("生成完整提示词，长度: {}", fullPrompt.length());

            org.springframework.ai.chat.model.ChatResponse response = chatModel.call(new Prompt(fullPrompt));
            String rawOutput = response.getResult().getOutput().getText();

            logger.debug("LLM 响应长度: {}", rawOutput.length());

            T result = outputConverter.convert(rawOutput);
            logger.debug("结构化输出转换成功，类型: {}", targetClass.getSimpleName());

            return result;
        } catch (IllegalArgumentException e) {
            logger.error("输入格式错误，无法进行结构化输出转换: {}", e.getMessage());
            throw new RuntimeException("输入参数无效，无法处理结构化输出: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            logger.error("LLM 响应格式不符合预期，无法解析: {}", e.getMessage());
            throw new RuntimeException("LLM 响应格式错误，无法解析结构化数据: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("结构化输出转换过程中发生未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("结构化输出转换失败: " + e.getMessage(), e);
        }
    }

    // 便捷方法 - 添加输入验证
    public WeatherData analyzeWeather(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("城市名称不能为空");
        }

        String prompt = String.format("Analyze the weather conditions for %s, including temperature, weather status, and forecast for the next few days", city.trim());
        return convertToStructure(prompt, WeatherData.class);
    }

    public TextAnalysis analyzeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("分析文本不能为空");
        }

        if (text.length() > 5000) { // 限制文本长度
            throw new IllegalArgumentException("分析文本过长，最大支持5000字符");
        }

        String prompt = String.format("Analyze the following text: %s\nExtract summary, keywords, sentiment, entities, and confidence score", text.trim());
        return convertToStructure(prompt, TextAnalysis.class);
    }

    // 添加一个安全的转换方法，不会抛出异常而是返回 null
    public <T> T convertToStructureSafe(String prompt, Class<T> targetClass) {
        try {
            return convertToStructure(prompt, targetClass);
        } catch (Exception e) {
            logger.warn("安全转换失败，返回 null: {}", e.getMessage());
            return null;
        }
    }
}