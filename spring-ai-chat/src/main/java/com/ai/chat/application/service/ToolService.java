package com.ai.chat.application.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 工具服务 - 提供 LLM 可调用的函数
 *
 * @author kiturone
 * @date 2026/4/4
 */
@Service
public class ToolService {

    private final Random random = new Random();

    @Tool(description = "获取当前时间，格式: yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "获取当前日期，格式: yyyy-MM-dd")
    public String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Tool(description = "计算数学表达式，例如: 2+3*4 或 (10-5)/2")
    public String calculateMath(@ToolParam(description = "数学表达式") String expression) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
            if (engine == null) {
                return "错误: 无法初始化计算引擎";
            }
            Object result = engine.eval(expression);
            return String.format("计算结果: %s = %s", expression, result);
        } catch (Exception e) {
            return "计算错误: " + e.getMessage();
        }
    }

    @Tool(description = "获取指定城市的天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        // 模拟天气查询
        int temperature = 15 + random.nextInt(20);
        String[] conditions = {"晴", "阴", "雨", "雪", "多云"};
        String condition = conditions[random.nextInt(conditions.length)];
        int humidity = 40 + random.nextInt(40);
        String wind = random.nextInt(30) + "km/h";

        return String.format(
            "城市: %s\n天气: %s\n温度: %d°C\n湿度: %d%%\n风速: %s",
            city, condition, temperature, humidity, wind
        );
    }

    @Tool(description = "生成随机数，参数: 最小值和最大值")
    public String generateRandomNumber(
            @ToolParam(description = "最小值") int min,
            @ToolParam(description = "最大值") int max) {
        if (min > max) {
            return "错误: 最小值不能大于最大值";
        }
        int randomNumber = min + random.nextInt(max - min + 1);
        return String.format("随机数(%d-%d): %d", min, max, randomNumber);
    }

    @Tool(description = "计算BMI指数，参数: 体重(kg)和身高(m)")
    public String calculateBMI(
            @ToolParam(description = "体重，单位: kg") double weight,
            @ToolParam(description = "身高，单位: m") double height) {
        if (weight <= 0 || height <= 0) {
            return "错误: 体重和身高必须为正数";
        }
        double bmi = weight / (height * height);
        String category;
        if (bmi < 18.5) {
            category = "偏瘦";
        } else if (bmi < 24) {
            category = "正常";
        } else if (bmi < 28) {
            category = "偏胖";
        } else {
            category = "肥胖";
        }
        return String.format("BMI指数: %.2f (%s)", bmi, category);
    }

    @Tool(description = "字符串反转")
    public String reverseString(@ToolParam(description = "要反转的字符串") String input) {
        if (input == null || input.isEmpty()) {
            return "错误: 输入字符串不能为空";
        }
        return new StringBuilder(input).reverse().toString();
    }

    @Tool(description = "计算字符串长度")
    public String getStringLength(@ToolParam(description = "要计算长度的字符串") String input) {
        if (input == null) {
            return "字符串长度: 0";
        }
        return String.format("字符串长度: %d", input.length());
    }
}