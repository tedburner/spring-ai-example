package com.ai.chat.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {
    private String city;
    private Double temperature;
    private String condition;
    private List<String> forecasts;  // 未来几天预报
    private Map<String, Object> details;  // 其他细节
}