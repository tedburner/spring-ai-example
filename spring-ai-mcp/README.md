# Spring AI MCP

Model Context Protocol (MCP) 实现模块。

## 已实现的 MCP 项目

- [x] **spring-ai-mcp-filesystem** - [本地文件读取服务器](./spring-ai-mcp-filesystem/README.md)（STDIO 传输）
- [x] **spring-ai-mcp-weather** - [天气查询服务器](./spring-ai-mcp-weather/README.md)（Streamable HTTP 传输）

## 子模块结构

```
spring-ai-mcp/
├── spring-ai-mcp-filesystem       # 本地文件读取 MCP Server
└── spring-ai-mcp-weather          # 天气查询 MCP Server
    ├── spring-ai-mcp-weather-client
    └── spring-ai-mcp-weather-server
```
