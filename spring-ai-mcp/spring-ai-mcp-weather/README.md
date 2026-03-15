# Spring AI MCP Weather

基于 Spring AI MCP 协议的天气查询服务器，使用 wttr.in 免费天气服务。

## 技术栈

- Java 17+
- Spring Boot 3.5.5
- Spring AI 2.0.0-M2
- wttr.in（免费天气 API，无需 Key）

## 模块结构

```
spring-ai-mcp-weather/
├── spring-ai-mcp-weather-client   # MCP 客户端
└── spring-ai-mcp-weather-server   # MCP 服务端（天气查询）
```

## 快速开始

### 1. 构建项目

```bash
cd spring-ai-mcp/spring-ai-mcp-weather
mvn clean package -DskipTests
```

### 2. 运行 Server

```bash
cd spring-ai-mcp-weather-server
mvn spring-boot:run
```

## MCP 配置

### Cherry Studio 配置

```json
{
  "mcpServers": {
    "weather": {
      "command": "java",
      "args": [
        "-jar",
        "D:/Project/Java/spring-ai-project/spring-ai-mcp/spring-ai-mcp-weather/spring-ai-mcp-weather-server/target/spring-ai-mcp-weather-server-1.0.0-SNAPSHOT.jar"
      ]
    }
  }
}
```

## 可用工具

- **weather_query**: 查询指定城市的天气信息
  - 参数：`city` - 城市名称（支持中文，如：北京、上海）
  - 返回：温度、天气状况、湿度等

## 使用示例

```
问：北京今天的天气怎么样？
问：上海明天会下雨吗？
```

## wttr.in 服务

[wttr.in](https://wttr.in/) 是一个免费的天气查询服务，无需 API Key 即可使用。

支持的查询格式：
- `wttr.in/北京` - 完整天气预报
- `wttr.in/北京?format=3` - 简洁格式（城市 + 温度 + 天气）
- `wttr.in/北京?lang=zh` - 中文输出
