package com.ai.chat.test.runner;

import com.ai.chat.test.unit.*;
import com.ai.chat.test.integration.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * 测试套件 - 运行所有单元和集成测试
 */
@Suite
@SelectClasses({
    // 单元测试
    ChatMemoryConfigTest.class,
    ChatMemoryTest.class,
    MetricsCollectorTest.class,
    StructuredOutputServiceTest.class,

    // 集成测试
    ChatControllerIntegrationTest.class,
    StructuredOutputControllerIntegrationTest.class,
    MetricsConfigurationTest.class,
    FullIntegrationTest.class
})
public class AllTestsSuite {
    // 这个类只是一个测试套件的入口点
}