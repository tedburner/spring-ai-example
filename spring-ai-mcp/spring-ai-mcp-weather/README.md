# Spring AI MCP Weather

基于 Spring AI MCP 协议的天气查询服务，整合 Ollama 本地大模型与 wttr.in 免费天气服务。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.5.5 |
| Spring AI | 1.1.3 |
| LLM | Ollama (qwen2.5:7b) |
| 天气服务 | wttr.in（免费，无需 API Key） |
| MCP 传输 | HTTP/SSE (Server-Sent Events) |

## 模块结构

```
spring-ai-mcp-weather/
├── spring-ai-mcp-weather-server   # MCP Server - 天气查询工具提供者
└── spring-ai-mcp-weather-client   # MCP Client - 整合 Ollama 模型
```

| 模块 | 端口 | 描述 |
|------|------|------|
| `spring-ai-mcp-weather-server` | 8181 | MCP Server，提供天气查询工具 |
| `spring-ai-mcp-weather-client` | 8300 | MCP Client + Chat API，整合 Ollama |

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

### 3. 构建项目

```bash
cd spring-ai-mcp/spring-ai-mcp-weather
mvn clean package -DskipTests
```

### 4. 启动服务（按顺序）

**步骤 1：启动 MCP Server（端口 8181）**
```bash
cd spring-ai-mcp-weather-server
mvn spring-boot:run
```

**步骤 2：启动 MCP Client（端口 8300）**
```bash
cd spring-ai-mcp-weather-client
mvn spring-boot:run
```

### 5. 测试

```bash
# 测试天气查询
curl "http://localhost:8300/weather-chat/query?query=What%20is%20the%20weather%20in%20Beijing%3F"
```

---

## MCP Server 配置

### 核心依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

### Server 配置 (`application-sse.yml`)

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
        instructions: "This server provides weather information tools and resources"
        stdio: false                    # false=启用 HTTP/SSE 传输
        capabilities:
          tool: true
          resource: true
          prompt: true
          completion: true
        sse-endpoint: /sse
        sse-message-endpoint: /mcp/message
        keep-alive-interval: 30s
```

### MCP 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/sse` | GET | SSE 连接端点，返回 session 和 message endpoint |
| `/mcp/message` | POST | 发送 MCP 消息（JSON-RPC 2.0） |

### 工具注册

**方式 1：@Tool 注解（推荐）**
```java
@Service
public class WeatherQueryTool {
    @Tool(description = "查询指定城市的天气信息，返回温度、湿度、风速等")
    public String queryWeather(String city) {
        // 使用 wttr.in 获取天气数据
    }
}
```

**方式 2：ToolCallbackProvider Bean**
```java
@Configuration
public class WeatherProviderConfig {
    @Bean
    public ToolCallbackProvider weatherTools(WeatherQueryTool weatherQueryTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherQueryTool)
                .build();
    }
}
```

### 可用工具

| 工具名 | 描述 | 参数 |
|--------|------|------|
| `queryWeather` | 查询指定城市的天气信息 | `city` - 城市名称（支持中文） |

---

## MCP Client 配置

### 核心依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
```

**为什么选择 WebFlux？**
- 响应式 SSE 连接，更好的流式支持
- 与 Spring AI 官方文档推荐一致
- 避免与项目中其他 WebMvc 依赖冲突

### Client 配置 (`application.yml`)

```yaml
server:
  port: 8300
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: qwen2.5:7b
          temperature: 0.5
    mcp:
      client:
        enabled: true
        name: mcp-weather-client
        version: 1.0.0
        type: SYNC
        request-timeout: 60s
        # SSE 传输配置
        sse:
          connections:
            weather-server:
              url: http://localhost:8181
              sse-endpoint: /sse
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
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
    }
}
```

**关键点：**
- ✅ 注入 `SyncMcpToolCallbackProvider` 而非 `ToolCallback[]`
- ✅ 使用 `getToolCallbacks()` 获取工具
- ✅ 模型配置使用 `application.yml`，不要硬编码

### API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/weather-chat/query` | GET | 非流式天气查询 |
| `/weather-chat/stream-query` | GET | 流式天气查询（SSE） |

---

## 架构说明

### 系统架构

```
┌──────────┐      HTTP/SSE      ┌─────────────┐      Ollama API    ┌──────────┐
│   User   │ ◄────────────────► │   Client    │ ◄──────────────►  │  Ollama  │
│          │    Port: 8300      │ (Port 8300) │    Port: 11434    │          │
└──────────┘                    └──────┬──────┘                    └──────────┘
                                       │
                                       │ HTTP/SSE (MCP)
                                       ▼
                                 ┌─────────────┐      HTTPS       ┌──────────┐
                                 │    Server   │ ◄──────────────► │ wttr.in  │
                                 │ (Port 8181) │                  │          │
                                 └─────────────┘                  └──────────┘
```

### 工具调用流程

1. 用户发送查询请求（如"What is the weather in Beijing?"）
2. Client 通过 SSE 连接到 Weather Server
3. Ollama 模型分析查询，判断需要调用天气工具
4. Client 调用 `queryWeather` 工具获取天气数据
5. Server 通过 wttr.in API 获取实时天气
6. 模型基于天气数据生成自然语言回复
7. 返回结果给用户

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

## 使用示例

### 基本天气查询

```bash
# 英文查询
问：What is the weather in Beijing today?
问：Shanghai weather now
问：How about Shenzhen weather?

# 工具调用验证
问：Use the weather tool to check Beijing weather
```

### 带上下文的查询

```bash
问：I want to travel to Beijing, what clothes should I bring? （模型会先查询天气）
问：Is it suitable for outdoor sports tomorrow? （模型会查询天气后判断）
```

---

## wttr.in 服务

[wttr.in](https://wttr.in/) 是一个免费的天气查询服务，无需 API Key。

**支持的查询格式：**

| 参数 | 说明 |
|------|------|
| `wttr.in/北京` | 完整天气预报 |
| `wttr.in/北京?format=3` | 简洁格式（城市 + 温度 + 天气） |
| `wttr.in/北京?lang=zh` | 中文输出 |
| `wttr.in/Beijing?lang=en` | 英文输出 |

---

## 在 MCP 客户端中配置

### Cherry Studio 配置

在 **设置 → MCP** 中添加：

```json
{
  "mcpServers": {
    "weather": {
      "url": "http://localhost:8181/sse",
      "transport": {
        "type": "sse"
      }
    }
  }
}
```

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

### Q: wttr.in 查询失败？

**可能原因：**
1. 网络连接问题
2. 城市名称不支持（尝试使用英文城市名）
3. 请求过于频繁被限流（wttr.in 免费服务有限制）

---

## 参考资源

- [Spring AI MCP 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol 规范](https://modelcontextprotocol.io/)
- [wttr.in 天气服务](https://wttr.in/)
- [Ollama 模型库](https://ollama.com/library)