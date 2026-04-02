package com.ai.chat.application.service;

import com.ai.chat.domain.entity.TextAnalysis;
import com.ai.chat.domain.entity.WeatherData;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructuredOutputService {

    @Autowired(required = false)
    private ChatModel chatModel;

    public <T> T convertToStructure(String prompt, Class<T> targetClass) {
        try {
            BeanOutputConverter<T> outputConverter = new BeanOutputConverter<>(targetClass);
            String formatInstructions = outputConverter.getFormat();

            // 构建带有格式要求的提示词
            String fullPrompt = String.format(
                "%s\n\n%s",
                prompt,
                formatInstructions
            );

            org.springframework.ai.chat.model.ChatResponse response = chatModel.call(new Prompt(fullPrompt));
            String rawOutput = response.getResult().getOutput().getText();

            return outputConverter.convert(rawOutput);
        } catch (Exception e) {
            throw new RuntimeException("Structured output conversion failed", e);
        }
    }

    // 便捷方法
    public WeatherData analyzeWeather(String city) {
        String prompt = String.format("Analyze the weather conditions for %s, including temperature, weather status, and forecast for the next few days", city);
        return convertToStructure(prompt, WeatherData.class);
    }

    public TextAnalysis analyzeText(String text) {
        String prompt = String.format("Analyze the following text: %s\nExtract summary, keywords, sentiment, entities, and confidence score", text);
        return convertToStructure(prompt, TextAnalysis.class);
    }
}