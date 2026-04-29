package com.ai.chat.application.advisor;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 包装现有 ToolCallback，在工具定义的 JSON Schema 中注入推理参数，
 * 让 LLM 在调用工具时输出 innerThought、confidence、memoryNotes。
 */
public class AugmentedToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final ToolDefinition augmentedDefinition;

    public AugmentedToolCallback(ToolCallback delegate) {
        this.delegate = delegate;
        this.augmentedDefinition = buildAugmentedDefinition(delegate.getToolDefinition());
    }

    private ToolDefinition buildAugmentedDefinition(ToolDefinition original) {
        return new ToolDefinition() {
            @Override
            public String name() {
                return original.name();
            }

            @Override
            public String description() {
                return original.description()
                        + "。调用此工具前，请在参数中提供 innerThought（你的推理过程）、"
                        + "confidence（对此工具调用的置信度 0.0-1.0）、"
                        + "memoryNotes（相关的对话上下文笔记）";
            }

            @Override
            public String inputSchema() {
                String originalSchema = original.inputSchema();
                if (originalSchema == null || originalSchema.isEmpty()) {
                    return defaultSchema();
                }
                // Inject extra properties into the JSON schema
                // Handle both "properties":{ and "properties": { patterns
                String extra = "\"innerThought\": {\"type\": \"string\", \"description\": \"调用此工具前的推理过程\"}, "
                                + "\"confidence\": {\"type\": \"number\", \"minimum\": 0.0, \"maximum\": 1.0, \"description\": \"对此工具调用的置信度\"}, "
                                + "\"memoryNotes\": {\"type\": \"string\", \"description\": \"相关的对话上下文笔记\"}, ";
                if (originalSchema.contains("\"properties\":{")) {
                    return originalSchema.replace("\"properties\":{", "\"properties\":{" + extra);
                }
                if (originalSchema.contains("\"properties\": {")) {
                    return originalSchema.replace("\"properties\": {", "\"properties\": {" + extra);
                }
                return originalSchema;
            }
        };
    }

    private String defaultSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "innerThought": {"type": "string", "description": "调用此工具前的推理过程"},
                    "confidence": {"type": "number", "minimum": 0.0, "maximum": 1.0, "description": "对此工具调用的置信度"},
                    "memoryNotes": {"type": "string", "description": "相关的对话上下文笔记"}
                  }
                }""";
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return augmentedDefinition;
    }

    @Override
    public String call(String toolInputArguments) {
        return delegate.call(toolInputArguments);
    }

    @Override
    public String call(String toolInputArguments, ToolContext toolContext) {
        return delegate.call(toolInputArguments, toolContext);
    }
}
