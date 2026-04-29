package com.ai.chat.application.service.dto;

/**
 * Routing 工作流结果：记录输入、分类决策和路由后的处理结果。
 */
public record RoutingDecision(
        String input,
        RouteClassification classification,
        String result
) {}
