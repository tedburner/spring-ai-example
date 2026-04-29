package com.ai.mcp.weather.server.controller;

import com.ai.mcp.weather.server.tool.DynamicToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ToolManagementController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ToolManagementControllerTest {

    @Mock
    private DynamicToolRegistry registry;

    private ToolManagementController controller;

    @BeforeEach
    void setUp() {
        controller = new ToolManagementController(registry);
    }

    @Test
    void testRegisterToolSuccess() {
        doNothing().when(registry).registerTool(any());

        var request = new ToolManagementController.ToolRegistrationRequest(
                "test_tool", "测试工具", "返回结果");
        Map<String, Object> response = controller.registerTool(request);

        assertTrue((Boolean) response.get("success"));
        verify(registry).registerTool(any());
    }

    @Test
    void testRegisterToolFailure() {
        doThrow(new IllegalArgumentException("工具已存在")).when(registry).registerTool(any());

        var request = new ToolManagementController.ToolRegistrationRequest(
                "existing_tool", "已存在", "返回结果");
        Map<String, Object> response = controller.registerTool(request);

        assertFalse((Boolean) response.get("success"));
        assertTrue(((String) response.get("message")).contains("工具已存在"));
    }

    @Test
    void testUnregisterToolSuccess() {
        doNothing().when(registry).unregisterTool("test_tool");

        Map<String, Object> response = controller.unregisterTool("test_tool");

        assertTrue((Boolean) response.get("success"));
        verify(registry).unregisterTool("test_tool");
    }

    @Test
    void testUnregisterToolFailure() {
        doThrow(new IllegalArgumentException("工具不存在")).when(registry).unregisterTool("non_existent");

        Map<String, Object> response = controller.unregisterTool("non_existent");

        assertFalse((Boolean) response.get("success"));
    }

    @Test
    void testListTools() {
        when(registry.listDynamicTools()).thenReturn(Map.of(
                "tool1", "工具1描述",
                "tool2", "工具2描述"
        ));

        Map<String, Object> response = controller.listTools();

        assertTrue((Boolean) response.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, String> tools = (Map<String, String>) response.get("tools");
        assertEquals(2, tools.size());
    }
}
