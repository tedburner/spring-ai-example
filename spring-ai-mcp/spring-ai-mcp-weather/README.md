# Spring AI MCP Weather

基于 Spring AI MCP 协议的天气查询服务，使用 wttr.in 免费天气服务。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.5.5 |
| Spring AI | 1.1.3 |
| 天气服务 | wttr.in（免费，无需 API Key） |
| 传输协议 | HTTP/SSE（Server-Sent Events） |

## 模块结构

| 模块 | 端口 | 描述 |
|------|------|------|
| `spring-ai-mcp-weather-server` | 8181 | MCP 服务端（天气查询） |
| `spring-ai-mcp-weather-client` | 8300 | MCP 客户端（HTTP 模式，整合 Ollama） |

## 快速开始

### 1. 构建项目

```bash
cd spring-ai-mcp/spring-ai-mcp-weather
mvn clean package -DskipTests
```

### 2. 启动服务（需要按顺序）

**先启动 Server（端口 8181）：**
```bash
cd spring-ai-mcp-weather-server
mvn spring-boot:run
```

**再启动 Client（端口 8300）：**
```bash
cd spring-ai-mcp-weather-client
mvn spring-boot:run
```

### 3. 测试 MCP Server 端点

**测试 SSE 连接：**
```bash
curl -N http://localhost:8181/sse
```

返回示例：
```
event:endpoint
data:/mcp/message?sessionId=bba2f501-f7e6-427a-9620-47d48cec2e48
```

### 4. 测试 Client API（可选）

**非流式查询：**
```bash
curl "http://localhost:8300/weather-chat/query?query=北京今天天气怎么样？"
```

**流式查询：**
```bash
curl "http://localhost:8300/weather-chat/stream-query?query=上海明天会下雨吗？"
```

## MCP 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/sse` | GET | SSE 连接端点，返回 session 和 message endpoint |
| `/mcp/message` | POST | 发送 MCP 消息（JSON-RPC 2.0） |

## 配置说明

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
        sse-message-endpoint: /mcp/message
        keep-alive-interval: 30s
```

### Client 配置 (`application.yml`)

```yaml
spring:
  ai:
    ollama:
      chat:
        options:
          model: deepseek-r1:1.5b
    mcp:
      client:
        transport:
          type: HTTP
          url: http://localhost:8181
```

## 架构说明

### HTTP/SSE 传输架构

```
┌─────────────┐      HTTP/SSE      ┌─────────────┐      HTTPS       ┌──────────┐
│  MCP Client │ ◄────────────────► │    Server   │ ◄──────────────► │ wttr.in  │
│ (如 Cherry) │   Port: 8181       │ (Port 8181) │                  │          │
└─────────────┘                    └─────────────┘                  └──────────┘
```

### MCP 工具调用流程

1. Client 连接 `/sse` 端点建立会话
2. Client 获取可用工具列表（`tools/list`）
3. 用户提问时，LLM 判断是否需要调用天气工具
4. Client 发送工具调用请求（`tools/call`）
5. Server 通过 wttr.in 获取天气数据
6. 结果返回给 Client 和 LLM 生成回复

## 可用工具

| 工具名 | 描述 | 参数 |
|--------|------|------|
| `queryWeather` | 查询指定城市的天气信息 | `city` - 城市名称（支持中文） |

## 使用示例

在 MCP 客户端（如 Cherry Studio）中提问：

```
问：北京今天的天气怎么样？
问：上海明天会下雨吗？
问：纽约现在多少度？
问：广州和深圳哪个更热？
```

## wttr.in 服务

[wttr.in](https://wttr.in/) 是一个免费的天气查询服务，无需 API Key。

**支持的查询格式：**

| 参数 | 说明 |
|------|------|
| `wttr.in/北京` | 完整天气预报 |
| `wttr.in/北京?format=3` | 简洁格式（城市 + 温度 + 天气） |
| `wttr.in/北京?lang=zh` | 中文输出 |
| `wttr.in/Beijing?lang=en` | 英文输出 |

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

## 常见问题

### Q: SSE 连接成功但无法调用工具？

**检查项：**
1. 确认 `stdio: false` 已设置
2. 确认工具已通过 `ToolCallbackProvider` 注册
3. 查看启动日志是否有 "Registered tools" 信息

### Q: wttr.in 查询失败？

**可能原因：**
1. 网络连接问题
2. 城市名称不支持（尝试使用英文城市名）
3. 请求过于频繁被限流（wttr.in 免费服务有限制）
