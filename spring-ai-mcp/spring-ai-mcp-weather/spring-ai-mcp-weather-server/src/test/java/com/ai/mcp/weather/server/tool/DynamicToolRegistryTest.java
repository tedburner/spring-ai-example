package com.ai.mcp.weather.server.tool;

import io.modelcontextprotocol.server.McpSyncServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DynamicToolRegistry 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DynamicToolRegistryTest {

    @Mock
    private McpSyncServer mcpSyncServer;

    private DynamicToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DynamicToolRegistry(mcpSyncServer);
    }

    private ToolCallback createMockTool() {
        ToolCallback tool = mock(ToolCallback.class);
        ToolDefinition definition = mock(ToolDefinition.class);
        when(definition.name()).thenReturn("dynamic_test_tool");
        when(definition.description()).thenReturn("动态测试工具");
        when(definition.inputSchema()).thenReturn("{\"type\":\"object\",\"properties\":{}}");
        when(tool.getToolDefinition()).thenReturn(definition);
        return tool;
    }

    @Test
    void testRegisterTool() {
        ToolCallback mockTool = createMockTool();
        assertDoesNotThrow(() -> registry.registerTool(mockTool));
        verify(mcpSyncServer).addTool(any());
    }

    @Test
    void testRegisterDuplicateToolThrows() {
        ToolCallback mockTool = createMockTool();
        registry.registerTool(mockTool);
        assertThrows(IllegalArgumentException.class, () -> registry.registerTool(mockTool));
    }

    @Test
    void testUnregisterTool() {
        ToolCallback mockTool = createMockTool();
        registry.registerTool(mockTool);
        assertDoesNotThrow(() -> registry.unregisterTool("dynamic_test_tool"));
        verify(mcpSyncServer).removeTool("dynamic_test_tool");
    }

    @Test
    void testUnregisterNonExistentToolThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.unregisterTool("non_existent"));
    }

    @Test
    void testListDynamicToolsEmpty() {
        Map<String, String> tools = registry.listDynamicTools();
        assertTrue(tools.isEmpty());
    }

    @Test
    void testListDynamicToolsAfterRegister() {
        ToolCallback mockTool = createMockTool();
        registry.registerTool(mockTool);
        Map<String, String> tools = registry.listDynamicTools();

        assertEquals(1, tools.size());
        assertTrue(tools.containsKey("dynamic_test_tool"));
        assertEquals("动态测试工具", tools.get("dynamic_test_tool"));
    }

    @Test
    void testRegisterAndUnregisterFlow() {
        ToolCallback mockTool = createMockTool();
        registry.registerTool(mockTool);
        assertEquals(1, registry.listDynamicTools().size());

        registry.unregisterTool("dynamic_test_tool");
        assertTrue(registry.listDynamicTools().isEmpty());
    }
}
