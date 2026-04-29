package com.ai.mcp.weather.server.controller;

import com.ai.mcp.weather.server.tool.DynamicToolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MCP 工具管理端点 — 运行时增删工具。
 */
@Slf4j
@RestController
@RequestMapping("/admin/tools")
public class ToolManagementController {

    private final DynamicToolRegistry registry;

    public ToolManagementController(DynamicToolRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/register")
    public Map<String, Object> registerTool(@RequestBody ToolRegistrationRequest request) {
        try {
            ToolCallback tool = FunctionToolCallback.builder(request.name(),
                            (Map<String, Object> args) -> request.response())
                    .description(request.description())
                    .inputType(Map.class)
                    .build();

            registry.registerTool(tool);
            return Map.of("success", true, "message", "工具已注册: " + request.name());
        } catch (Exception e) {
            log.error("工具注册失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/{toolName}")
    public Map<String, Object> unregisterTool(@PathVariable String toolName) {
        try {
            registry.unregisterTool(toolName);
            return Map.of("success", true, "message", "工具已注销: " + toolName);
        } catch (Exception e) {
            log.error("工具注销失败", e);
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/list")
    public Map<String, Object> listTools() {
        return Map.of("success", true, "tools", registry.listDynamicTools());
    }

    public record ToolRegistrationRequest(
            String name,
            String description,
            String response
    ) {}
}
