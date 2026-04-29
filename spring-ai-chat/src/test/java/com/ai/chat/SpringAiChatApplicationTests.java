package com.ai.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 集成测试 — 需要 Ollama 运行。
 */
@SpringBootTest
@Disabled("需要外部服务：Ollama (端口 11434)")
class SpringAiChatApplicationTests {

    @Test
    void contextLoads() {
    }

}
