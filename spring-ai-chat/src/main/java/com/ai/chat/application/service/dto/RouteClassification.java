package com.ai.chat.application.service.dto;

/**
 * 路由分类决策：记录推理过程和选中的路由。
 */
public record RouteClassification(
        String reasoning,
        String selection
) {}
