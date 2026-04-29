package com.ai.mcp.weather.server.tool;

import io.modelcontextprotocol.server.McpSyncServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 运行时动态注册/注销 MCP 工具。通过 HTTP 端点触发，无需重启服务。
 */
@Slf4j
@Service
public class DynamicToolRegistry {

    private final McpSyncServer mcpSyncServer;
    private final Map<String, ToolCallback> dynamicTools = new ConcurrentHashMap<>();

    public DynamicToolRegistry(McpSyncServer mcpSyncServer) {
        this.mcpSyncServer = mcpSyncServer;
    }

    /**
     * 注册单个工具
     */
    public void registerTool(ToolCallback tool) {
        String name = tool.getToolDefinition().name();
        if (dynamicTools.containsKey(name)) {
            throw new IllegalArgumentException("工具已存在: " + name);
        }
        dynamicTools.put(name, tool);
        mcpSyncServer.addTool(McpToolUtils.toSyncToolSpecification(tool));
        log.info("动态注册工具: {}", name);
    }

    /**
     * 注销指定工具
     */
    public void unregisterTool(String toolName) {
        if (!dynamicTools.containsKey(toolName)) {
            throw new IllegalArgumentException("工具不存在: " + toolName);
        }
        dynamicTools.remove(toolName);
        mcpSyncServer.removeTool(toolName);
        log.info("动态注销工具: {}", toolName);
    }

    /**
     * 列出所有动态注册的工具
     */
    public Map<String, String> listDynamicTools() {
        return dynamicTools.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getToolDefinition().description()
                ));
    }
}
