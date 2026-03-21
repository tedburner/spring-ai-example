# Spring AI MCP

Model Context Protocol (MCP) 实现模块，提供基于 Spring AI 的 MCP Server 和 Client 实现。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.5.5 |
| Spring AI | 1.1.3 |
| MCP 传输 | SSE (Server-Sent Events) |

## 已实现的项目

- ✅ **spring-ai-mcp-filesystem** - [本地文件读取服务器](./spring-ai-mcp-filesystem/README.md)（STDIO 传输）
- ✅ **spring-ai-mcp-weather** - [天气查询项目](./spring-ai-mcp-weather/README.md)（SSE 传输）

## 子模块结构

```
spring-ai-mcp/
├── spring-ai-mcp-filesystem           # 本地文件读取 MCP Server (STDIO)
└── spring-ai-mcp-weather              # 天气查询 MCP 项目 (SSE)
    ├── spring-ai-mcp-weather-server   # MCP Server (端口 8181)
    └── spring-ai-mcp-weather-client   # MCP Client + Ollama (端口 8300)
```

**端口分配：**

| 模块 | 端口 | 说明 |
|------|------|------|
| weather-server | 8181 | MCP Server，提供天气查询工具 |
| weather-client | 8300 | MCP Client，整合 Ollama 模型 |

---

## 快速开始

### 1. 环境要求

- **Java 17+**
- **Ollama** 已安装并运行（端口 11434）
- 支持 Function Calling 的模型：
  - 推荐：`qwen2.5:7b` 或 `qwen2.5:14b`
  - 可选：`llama3.1:8b`

### 2. 安装模型

```bash
# 拉取支持工具调用的模型
ollama pull qwen2.5:7b
```

### 3. 启动服务

```bash
# 步骤 1: 启动 MCP Server (端口 8181)
cd spring-ai-mcp-weather/spring-ai-mcp-weather-server
mvn spring-boot:run

# 步骤 2: 启动 MCP Client (端口 8300)
cd spring-ai-mcp-weather/spring-ai-mcp-weather-client
mvn spring-boot:run
```

### 4. 测试

```bash
# 测试天气查询
curl "http://localhost:8300/weather-chat/query?query=What%20is%20the%20weather%20in%20Beijing%3F"
```

---

## MCP Client 使用指南

### 核心依赖

| 依赖 | 传输类型 | 适用场景 |
|------|---------|---------|
| `spring-ai-starter-mcp-client-webflux` | SSE (WebFlux) | 推荐：响应式应用 |
| `spring-ai-starter-mcp-client` | SSE (HttpClient) | 标准客户端 |

**推荐使用 WebFlux 依赖：**
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
```

### 配置方式

**application.yml 配置：**
```yaml
spring:
  ai:
    ollama:
      chat:
        options:
          model: qwen2.5:7b       # 支持工具调用的模型
          temperature: 0.5
    mcp:
      client:
        enabled: true
        type: SYNC                # SYNC 或 ASYNC
        sse:
          connections:
            weather-server:       # 连接名称（自定义）
              url: http://localhost:8181
              sse-endpoint: /sse   # Server 的 SSE 端点
```

### 工具注入方式

**正确注入方式：**
```java
@Service
public class WeatherChatService {
    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    public WeatherChatService(SyncMcpToolCallbackProvider toolCallbackProvider) {
        this.toolCallbackProvider = toolCallbackProvider;

        // 获取工具回调
        var toolCallbacks = toolCallbackProvider.getToolCallbacks();
        // 使用工具...
    }
}
```

**使用工具：**
```java
var options = OllamaChatOptions.builder()
        .toolCallbacks(toolCallbackProvider.getToolCallbacks())
        .build();

ChatResponse response = ollamaChatModel.call(new Prompt(query, options));
```

**关键点：**
- ✅ 注入 `SyncMcpToolCallbackProvider`
- ✅ 使用 `getToolCallbacks()` 获取工具
- ✅ 模型配置使用 `application.yml`，不要硬编码

---

## MCP Server 配置

### Server 依赖

| 依赖 | 传输类型 | 适用场景 |
|------|---------|---------|
| `spring-ai-starter-mcp-server-webmvc` | SSE (WebMvc) | 推荐：Spring Web 项目 |
| `spring-ai-starter-mcp-server-webflux` | SSE (WebFlux) | 响应式项目 |
| `spring-ai-starter-mcp-server` | STDIO | 命令行工具 |

### Server 配置示例

```yaml
server:
  port: 8181
spring:
  ai:
    mcp:
      server:
        name: weather-mcp-server
        version: 1.0.0
        type: SYNC
        stdio: false              # false=启用 HTTP/SSE
        capabilities:
          tool: true
          resource: true
          prompt: true
          completion: true
        sse-endpoint: /sse
        sse-message-endpoint: /mcp/message
        keep-alive-interval: 30s
```

### 工具注册方式

**方式 1：@Tool 注解（推荐）**
```java
@Service
public class WeatherService {
    @Tool(description = "查询指定城市的天气信息")
    public String queryWeather(String city) {
        // 实现逻辑
    }
}
```

**方式 2：ToolCallbackProvider Bean**
```java
@Configuration
public class WeatherProviderConfig {
    @Bean
    public ToolCallbackProvider weatherTools(WeatherQueryTool tool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tool)
                .build();
    }
}
```

---

## 测试用例

### 1. 测试 MCP Server

**测试 SSE 连接：**
```bash
curl -N http://localhost:8181/sse
```

**预期返回：**
```
event:endpoint
data:/mcp/message?sessionId=bba2f501-f7e6-427a-9620-47d48cec2e48
```

### 2. 测试 MCP Client

**非流式查询：**
```bash
curl "http://localhost:8300/weather-chat/query?query=What%20is%20the%20weather%20in%20Beijing%3F"
```

**预期结果：**
- 模型成功调用 `queryWeather` 工具
- 返回实时天气数据

**流式查询：**
```bash
curl -N "http://localhost:8300/weather-chat/stream-query?query=Shanghai%20weather"
```

**预期结果：**
- SSE 流式返回天气数据
- 模型基于实时数据生成回复

### 3. 测试不同城市

```bash
# 测试北京
curl "http://localhost:8300/weather-chat/query?query=Beijing%20weather"

# 测试上海
curl "http://localhost:8300/weather-chat/query?query=Shanghai%20weather"

# 测试深圳
curl "http://localhost:8300/weather-chat/query?query=Shenzhen%20weather"
```

---

## 测试结果

### 功能测试

| 测试项 | 结果 | 说明 |
|--------|------|------|
| Server SSE 连接 | ✅ 通过 | 成功返回 sessionId |
| Client 工具注入 | ✅ 通过 | 正确注入 SyncMcpToolCallbackProvider |
| 非流式查询 | ✅ 通过 | 模型成功调用工具获取天气 |
| 流式查询 | ✅ 通过 | 流式返回实时天气数据 |
| 多城市测试 | ✅ 通过 | 支持多城市天气查询 |

### 测试数据

| 城市 | 温度 | 天气 |
|------|------|------|
| Beijing | 10°C | Partly cloudy |
| Shanghai | 11°C | Sunny |
| Shenzhen | 23°C | Sunny |

---

## 注意事项

### 1. 依赖选择问题

**问题**：项目中已有 `spring-boot-starter-web`，应选择哪个 MCP Client 依赖？

**解决方案**：
- ✅ 使用 `spring-ai-starter-mcp-client-webflux`
- WebFlux 提供响应式 SSE 连接，与 WebMvc 共存

### 2. 工具注入问题

**问题**：工具未被调用，模型直接返回通用回答

**原因**：错误地注入 `ToolCallback[]` 而非 `SyncMcpToolCallbackProvider`

**解决方案**：
```java
// ❌ 错误方式
public WeatherChatService(ToolCallback[] toolCallbacks) { }

// ✅ 正确方式
public WeatherChatService(SyncMcpToolCallbackProvider toolCallbackProvider) { }
```

### 3. 模型选择问题

**问题**：模型不调用工具

**原因**：某些模型不支持 Function Calling

**解决方案**：
- ✅ 使用支持工具调用的模型：`qwen2.5:7b`, `llama3.1:8b`
- ❌ 避免使用：`deepseek-r1:1.5b` (不支持工具调用)

### 4. 配置方式问题

**问题**：使用旧的 `transport.type` 配置导致连接失败

**解决方案**：
```yaml
# ❌ 旧配置（不推荐）
transport:
  type: HTTP
  url: http://localhost:8181

# ✅ 新配置（推荐）
sse:
  connections:
    weather-server:
      url: http://localhost:8181
      sse-endpoint: /sse
```

### 5. 模型配置管理

**问题**：代码中硬编码模型名称，难以切换

**最佳实践**：
- ✅ 使用 `application.yml` 统一管理模型配置
- ✅ Spring AI 自动注入配置到 `OllamaChatModel`
- ❌ 避免在代码中硬编码 `model` 和 `temperature`

**示例**：
```yaml
spring:
  ai:
    ollama:
      chat:
        options:
          model: qwen2.5:7b
          temperature: 0.5
```

---

## 常见问题

### Q: 如何确认工具是否成功加载？

**检查 Client 启动日志：**
```
MCP 工具已加载：1 个
  - 工具：queryWeather
```

如果没有这个日志，说明工具注入失败，检查配置和依赖。

### Q: 为什么模型不调用工具？

**可能原因：**
1. 模型不支持 Function Calling（更换模型）
2. 工具未正确注入（检查 `SyncMcpToolCallbackProvider`）
3. Server 未启动或配置错误（检查 SSE 连接）

### Q: 如何查看详细的 MCP 通信日志？

**启动时添加调试参数：**
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Ddebug=true"
```

### Q: 如何测试 MCP Server 是否正常工作？

**测试 SSE 端点：**
```bash
curl -N http://localhost:8181/sse
```

成功返回 `event:endpoint` 和 sessionId 表示 Server 正常。

---

## 参考资源

- [Spring AI MCP 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol 规范](https://modelcontextprotocol.io/)
- [wttr.in 天气服务](https://wttr.in/)