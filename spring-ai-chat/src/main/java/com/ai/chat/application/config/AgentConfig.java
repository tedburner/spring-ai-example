package com.ai.chat.application.config;

import com.ai.chat.application.service.AgentService;
import com.ai.chat.application.service.ToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 配置类
 *
 * @author kiturone
 * @date 2026/04/10
 */
@Configuration
public class AgentConfig {

    /**
     * 创建 ChatClient Bean
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * 工具回调提供者 - 注册 ToolService 中的所有工具
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(ToolService toolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(toolService)
                .build();
    }

    /**
     * Agent 服务 Bean
     */
    @Bean
    public AgentService agentService(ChatClient.Builder builder,
                                    ToolCallbackProvider toolCallbackProvider) {
        return new AgentService(builder, java.util.List.of(toolCallbackProvider), null);
    }
}
