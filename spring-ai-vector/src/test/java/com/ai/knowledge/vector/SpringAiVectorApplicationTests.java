package com.ai.knowledge.vector;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 集成测试 — 需要 Ollama + Elasticsearch 运行。
 * 手动启动外部服务后取消 @Disabled。
 */
@SpringBootTest
@Disabled("需要外部服务：Ollama (端口 11434) + Elasticsearch (端口 9200)")
class SpringAiVectorApplicationTests {

    @Test
    void contextLoads() {
    }

}
