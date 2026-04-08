package com.ai.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.ai.chat.application.config.ChatMemoryProperties;
import com.ai.chat.application.config.PromptTemplateProperties;

/**
 * 大模型对话测试服务
 *
 * @author kiturone
 * @date 2025/5/2 18:05
 */
@SpringBootApplication
@EnableConfigurationProperties({ChatMemoryProperties.class, PromptTemplateProperties.class})
public class SpringAiChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiChatApplication.class, args);
    }

}
