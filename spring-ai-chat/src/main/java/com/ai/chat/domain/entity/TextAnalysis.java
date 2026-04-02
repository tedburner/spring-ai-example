package com.ai.chat.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextAnalysis {
    private String summary;
    private List<String> keywords;
    private String sentiment;
    private Map<String, Integer> entities;
    private Double confidence;
}