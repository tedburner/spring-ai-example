package com.ai.chat.application.advisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AugmentedToolCallback 和 AugmentedToolCallbackProvider 单元测试
 */
class AugmentedToolCallbackTest {

    private ToolCallback mockDelegate;
    private ToolDefinition mockDefinition;

    @BeforeEach
    void setUp() {
        mockDefinition = mock(ToolDefinition.class);
        when(mockDefinition.name()).thenReturn("test_tool");
        when(mockDefinition.description()).thenReturn("测试工具");
        when(mockDefinition.inputSchema()).thenReturn(
                "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\",\"description\":\"查询词\"}},\"required\":[\"query\"]}"
        );

        mockDelegate = mock(ToolCallback.class);
        when(mockDelegate.getToolDefinition()).thenReturn(mockDefinition);
        when(mockDelegate.call(anyString())).thenReturn("测试结果");
        when(mockDelegate.call(anyString(), any(ToolContext.class))).thenReturn("测试结果");
    }

    @Test
    void testAugmentedToolCallbackDefinition() {
        AugmentedToolCallback augmented = new AugmentedToolCallback(mockDelegate);

        assertEquals("test_tool", augmented.getToolDefinition().name());
        assertTrue(augmented.getToolDefinition().description().contains("innerThought"));
        assertTrue(augmented.getToolDefinition().description().contains("confidence"));
        assertTrue(augmented.getToolDefinition().description().contains("memoryNotes"));
    }

    @Test
    void testAugmentedToolCallbackInputSchema() {
        AugmentedToolCallback augmented = new AugmentedToolCallback(mockDelegate);

        String schema = augmented.getToolDefinition().inputSchema();
        assertNotNull(schema);
        assertTrue(schema.contains("innerThought"));
        assertTrue(schema.contains("confidence"));
        assertTrue(schema.contains("memoryNotes"));
        // 原始参数应保留
        assertTrue(schema.contains("query"));
    }

    @Test
    void testAugmentedToolCallbackCallDelegates() {
        AugmentedToolCallback augmented = new AugmentedToolCallback(mockDelegate);

        String result = augmented.call("{\"query\":\"test\"}");

        assertEquals("测试结果", result);
        verify(mockDelegate).call("{\"query\":\"test\"}");
    }

    @Test
    void testAugmentedToolCallbackCallWithContextDelegates() {
        AugmentedToolCallback augmented = new AugmentedToolCallback(mockDelegate);
        ToolContext context = new ToolContext(java.util.Map.of("key", "value"));

        String result = augmented.call("{\"query\":\"test\"}", context);

        assertEquals("测试结果", result);
        verify(mockDelegate).call("{\"query\":\"test\"}", context);
    }

    @Test
    void testAugmentedToolCallbackWithEmptySchema() {
        when(mockDefinition.inputSchema()).thenReturn("");

        AugmentedToolCallback augmented = new AugmentedToolCallback(mockDelegate);

        String schema = augmented.getToolDefinition().inputSchema();
        assertNotNull(schema);
        assertTrue(schema.contains("innerThought"));
        assertTrue(schema.contains("confidence"));
    }

    @Test
    void testAugmentedToolCallbackWithNullSchema() {
        when(mockDefinition.inputSchema()).thenReturn(null);

        AugmentedToolCallback augmented = new AugmentedToolCallback(mockDelegate);

        String schema = augmented.getToolDefinition().inputSchema();
        assertNotNull(schema);
        assertTrue(schema.contains("innerThought"));
    }

    @Test
    void testAugmentedToolCallbackProviderWrapsAllCallbacks() {
        ToolCallback mockCallback1 = mockDelegate;
        ToolCallback mockCallback2 = mock(ToolCallback.class);
        when(mockCallback2.getToolDefinition()).thenReturn(mockDefinition);
        when(mockCallback2.call(anyString())).thenReturn("结果2");

        ToolCallbackProvider mockProvider = mock(ToolCallbackProvider.class);
        when(mockProvider.getToolCallbacks()).thenReturn(new ToolCallback[]{mockCallback1, mockCallback2});

        AugmentedToolCallbackProvider augmentedProvider = new AugmentedToolCallbackProvider(mockProvider);

        ToolCallback[] results = augmentedProvider.getToolCallbacks();

        assertEquals(2, results.length);
        assertTrue(results[0] instanceof AugmentedToolCallback);
        assertTrue(results[1] instanceof AugmentedToolCallback);
    }

    @Test
    void testAugmentedToolCallbackProviderWithEmptyCallbacks() {
        ToolCallbackProvider mockProvider = mock(ToolCallbackProvider.class);
        when(mockProvider.getToolCallbacks()).thenReturn(new ToolCallback[0]);

        AugmentedToolCallbackProvider augmentedProvider = new AugmentedToolCallbackProvider(mockProvider);

        ToolCallback[] results = augmentedProvider.getToolCallbacks();

        assertEquals(0, results.length);
    }
}
