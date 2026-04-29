package com.ai.common;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("集成测试 — 启动完整 Spring 上下文较慢，按需执行")
class CommonApplicationTests {

    @Test
    void contextLoads() {
    }

}
