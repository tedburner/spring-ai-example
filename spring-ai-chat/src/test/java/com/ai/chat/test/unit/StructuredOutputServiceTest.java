package com.ai.chat.test.unit;

import com.ai.chat.application.service.StructuredOutputService;
import com.ai.chat.domain.entity.TextAnalysis;
import com.ai.chat.domain.entity.WeatherData;
import com.ai.chat.test.mock.MockChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StructuredOutputServiceTest {

    private StructuredOutputService structuredOutputService;

    @BeforeEach
    void setUp() {
        structuredOutputService = new StructuredOutputService();
        // 设置 mock 的 chat model
        MockChatModel mockChatModel = new MockChatModel("{\n" +
                "  \"city\": \"Beijing\",\n" +
                "  \"temperature\": 25.5,\n" +
                "  \"condition\": \"Sunny\",\n" +
                "  \"forecasts\": [\"Tomorrow Sunny\", \"Next Day Cloudy\"],\n" +
                "  \"details\": {\n" +
                "    \"humidity\": 60,\n" +
                "    \"wind\": \"5km/h\"\n" +
                "  }\n" +
                "}");
        structuredOutputService.chatModel = mockChatModel;
    }

    @Test
    void testConvertToWeatherData() {
        String prompt = "Analyze weather for Beijing";

        // 创建符合 JSON 格式的响应
        MockChatModel mockChatModel = new MockChatModel("{\n" +
                "  \"city\": \"Beijing\",\n" +
                "  \"temperature\": 25.5,\n" +
                "  \"condition\": \"Sunny\",\n" +
                "  \"forecasts\": [\"Tomorrow Sunny\", \"Next Day Cloudy\"],\n" +
                "  \"details\": {\n" +
                "    \"humidity\": 60,\n" +
                "    \"wind\": \"5km/h\"\n" +
                "  }\n" +
                "}");
        structuredOutputService.chatModel = mockChatModel;

        WeatherData result = structuredOutputService.convertToStructure(prompt, WeatherData.class);

        assertNotNull(result);
        assertEquals("Beijing", result.getCity());
        assertEquals(25.5, result.getTemperature());
        assertEquals("Sunny", result.getCondition());
        assertEquals(2, result.getForecasts().size());
        assertEquals("Tomorrow Sunny", result.getForecasts().get(0));
        assertNotNull(result.getDetails());
        assertEquals(60, result.getDetails().get("humidity"));
    }

    @Test
    void testConvertToTextAnalysis() {
        String prompt = "Analyze text: Hello world";

        // 创建符合 JSON 格式的响应
        MockChatModel mockChatModel = new MockChatModel("{\n" +
                "  \"summary\": \"A greeting\",\n" +
                "  \"keywords\": [\"hello\", \"world\"],\n" +
                "  \"sentiment\": \"positive\",\n" +
                "  \"entities\": {\n" +
                "    \"hello\": 1,\n" +
                "    \"world\": 1\n" +
                "  },\n" +
                "  \"confidence\": 0.95\n" +
                "}");
        structuredOutputService.chatModel = mockChatModel;

        TextAnalysis result = structuredOutputService.convertToStructure(prompt, TextAnalysis.class);

        assertNotNull(result);
        assertEquals("A greeting", result.getSummary());
        assertEquals(Arrays.asList("hello", "world"), result.getKeywords());
        assertEquals("positive", result.getSentiment());
        assertEquals(0.95, result.getConfidence());
        assertEquals(2, result.getEntities().size());
        assertEquals(Integer.valueOf(1), result.getEntities().get("hello"));
        assertEquals(Integer.valueOf(1), result.getEntities().get("world"));
    }

    @Test
    void testAnalyzeWeatherConvenienceMethod() {
        // 使用不同的 mock 响应
        MockChatModel mockChatModel = new MockChatModel("{\n" +
                "  \"city\": \"Shanghai\",\n" +
                "  \"temperature\": 22.0,\n" +
                "  \"condition\": \"Cloudy\",\n" +
                "  \"forecasts\": [\"Rain tomorrow\"],\n" +
                "  \"details\": {}\n" +
                "}");
        structuredOutputService.chatModel = mockChatModel;

        WeatherData result = structuredOutputService.analyzeWeather("Shanghai");

        assertNotNull(result);
        assertEquals("Shanghai", result.getCity());
        assertEquals(22.0, result.getTemperature());
        assertEquals("Cloudy", result.getCondition());
    }

    @Test
    void testAnalyzeTextConvenienceMethod() {
        MockChatModel mockChatModel = new MockChatModel("{\n" +
                "  \"summary\": \"Test summary\",\n" +
                "  \"keywords\": [\"test\", \"keywords\"],\n" +
                "  \"sentiment\": \"neutral\",\n" +
                "  \"entities\": {},\n" +
                "  \"confidence\": 0.8\n" +
                "}");
        structuredOutputService.chatModel = mockChatModel;

        TextAnalysis result = structuredOutputService.analyzeText("Test text for analysis");

        assertNotNull(result);
        assertEquals("Test summary", result.getSummary());
        assertEquals("neutral", result.getSentiment());
        assertEquals(0.8, result.getConfidence());
    }

    @Test
    void testInvalidJsonHandling() {
        MockChatModel mockChatModel = new MockChatModel("Invalid JSON response");
        structuredOutputService.chatModel = mockChatModel;

        assertThrows(RuntimeException.class, () -> {
            structuredOutputService.convertToStructure("test", WeatherData.class);
        });
    }

    @Test
    void testPartialJsonHandling() {
        // 部分字段缺失的 JSON
        MockChatModel mockChatModel = new MockChatModel("{\n" +
                "  \"city\": \"Beijing\"\n" +
                "  // 其他字段缺失\n" +
                "}");
        structuredOutputService.chatModel = mockChatModel;

        WeatherData result = structuredOutputService.convertToStructure("test", WeatherData.class);

        assertNotNull(result);
        assertEquals("Beijing", result.getCity());
        assertNull(result.getTemperature()); // 缺失字段应为 null
        assertNull(result.getCondition());
        assertNull(result.getForecasts());
        assertNull(result.getDetails());
    }
}