# Maven Surefire Plugin 配置用于运行测试
# 在项目根目录执行: mvn test

# 测试类命名规范:
# *Test.java, *Tests.java, *TestCase.java, *IntegrationTest.java

# 主要测试覆盖范围:
# 1. ChatMemory 功能测试 - 验证会话记忆功能
# 2. MetricsCollector 功能测试 - 验证监控指标收集
# 3. StructuredOutput 功能测试 - 验证结构化输出
# 4. 控制器端到端测试 - 验证API接口
# 5. 集成测试 - 验证整体功能协同