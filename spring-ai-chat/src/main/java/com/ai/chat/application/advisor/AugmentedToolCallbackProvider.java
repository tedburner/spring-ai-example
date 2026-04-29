package com.ai.chat.application.advisor;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.Arrays;

/**
 * 包装现有 ToolCallbackProvider，将所有工具定义增强为包含推理参数。
 */
public class AugmentedToolCallbackProvider implements ToolCallbackProvider {

    private final ToolCallbackProvider delegate;

    public AugmentedToolCallbackProvider(ToolCallbackProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return Arrays.stream(delegate.getToolCallbacks())
                .map(AugmentedToolCallback::new)
                .toArray(ToolCallback[]::new);
    }
}
