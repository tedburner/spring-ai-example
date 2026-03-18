# Spring AI MCP Weather

基于 Spring AI MCP 协议的天气查询服务，使用 wttr.in 免费天气服务。

## 技术栈

- Java 17+
- Spring Boot 3.5.5
- Spring AI 2.0.0-M2
- wttr.in（免费天气 API，无需 Key）
- HTTP Streamable 传输（SSE）

## 模块结构

```
spring-ai-mcp-weather/
├── spring-ai-mcp-weather-client   # MCP 客户端（HTTP 模式）
│   ├── McpClientConfig            # MCP 客户端配置
│   ├── WeatherChatService         # 聊天服务（整合 MCP + Ollama）
│   └── WeatherChatController      # REST API
└── spring-ai-mcp-weather-server   # MCP 服务端（天气查询）
    └── WeatherQueryTool           # 天气查询工具
```

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

### 3. 测试 API

**非流式查询：**
```bash
curl "http://localhost:8300/weather-chat/query?query=北京今天天气怎么样？"
```

**流式查询：**
```bash
curl "http://localhost:8300/weather-chat/stream-query?query=上海明天会下雨吗？"
```

## 架构说明

### HTTP Streamable 传输

```
┌─────────────┐      HTTP/SSE      ┌─────────────┐      HTTPS       ┌──────────┐
│   Client    │ ◄────────────────► │    Server   │ ◄──────────────► │ wttr.in  │
│  (Port 8300)│   Port: 8181       │ (Port 8181) │                  │          │
└─────────────┘                    └─────────────┘                  └──────────┘
```

### MCP 工具调用流程

1. Client 从 Server 获取可用工具列表
2. 用户提问时，Ollama 模型判断是否需要调用工具
3. 如需天气查询，调用 `weather_query` 工具
4. Server 通过 wttr.in 获取天气数据
5. 结果返回给模型生成最终回复

## API 端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `/weather-chat/query` | GET | 非流式天气查询 |
| `/weather-chat/stream-query` | GET | 流式天气查询（SSE） |

## 配置说明

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

### Server 配置 (`application.yml`)
```yaml
server:
  port: 8181
spring:
  ai:
    mcp:
      server:
        transport:
          type: WEBFLUX
```

## 可用工具

- **weather_query**: 查询指定城市的天气信息
  - 参数：`city` - 城市名称（支持中文，如：北京、上海）
  - 返回：温度、天气状况、湿度等

## 使用示例

```
问：北京今天的天气怎么样？
问：上海明天会下雨吗？
问：纽约现在多少度？
```

## wttr.in 服务

[wttr.in](https://wttr.in/) 是一个免费的天气查询服务，无需 API Key 即可使用。

支持的查询格式：
- `wttr.in/北京` - 完整天气预报
- `wttr.in/北京?format=3` - 简洁格式（城市 + 温度 + 天气）
- `wttr.in/北京?lang=zh` - 中文输出
