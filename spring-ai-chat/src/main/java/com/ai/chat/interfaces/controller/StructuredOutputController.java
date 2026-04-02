package com.ai.chat.interfaces.controller;

import com.ai.chat.domain.entity.TextAnalysis;
import com.ai.chat.domain.entity.WeatherData;
import com.ai.chat.application.service.StructuredOutputService;
import com.ai.common.http.WebResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/structured")
public class StructuredOutputController {

    private final StructuredOutputService structuredOutputService;

    public StructuredOutputController(StructuredOutputService structuredOutputService) {
        this.structuredOutputService = structuredOutputService;
    }

    @PostMapping("/weather")
    public WebResult getWeatherData(@RequestBody Map<String, String> request) {
        String city = request.get("city");
        WeatherData weatherData = structuredOutputService.analyzeWeather(city);
        return WebResult.buildSuccess(weatherData);
    }

    @PostMapping("/analyze")
    public WebResult analyzeText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        TextAnalysis analysis = structuredOutputService.analyzeText(text);
        return WebResult.buildSuccess(analysis);
    }

    // 通用结构化输出接口
    @PostMapping("/convert")
    public WebResult convertGeneric(@RequestBody GenericConversionRequest request) {
        // 这里简化处理，实际应用中需要更复杂的动态类型处理
        try {
            // 根据请求的目标类型进行相应处理
            if ("weather".equalsIgnoreCase(request.getTargetType())) {
                return WebResult.buildSuccess(structuredOutputService.analyzeWeather(request.getInput()));
            } else if ("text".equalsIgnoreCase(request.getTargetType())) {
                return WebResult.buildSuccess(structuredOutputService.analyzeText(request.getInput()));
            } else {
                return WebResult.buildFail("Unsupported target type: " + request.getTargetType());
            }
        } catch (Exception e) {
            return WebResult.buildFail("Conversion failed: " + e.getMessage());
        }
    }

    // 通用转换请求类
    public static class GenericConversionRequest {
        private String input;
        private String targetType; // weather, text, etc.
        private String prompt;

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }
}