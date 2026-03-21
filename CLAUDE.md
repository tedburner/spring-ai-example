# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build all modules
mvn clean package

# Build skipping tests
mvn clean package -Dmaven.test.skip=true

# Run a single module
cd <module-name> && mvn spring-boot:run
```

## Project Structure

This is a multi-module Maven project containing Spring AI examples and integrations. Spring Boot 3.5.5 and Spring AI 2.0.0-M2 are used across all modules.

### Module Organization

**Core Modules:**
- `spring-ai-common` - Shared utilities for AI modules (Jackson, Lombok)
- `spring-ai-chat` - LLM chat integration with Ollama (streaming and non-streaming)
- `spring-ai-vector` - Vector store with Elasticsearch, PDF document reader, RAG
- `spring-ai-openmanus` - OpenManus integration
- `spring-ai-mcp` - Model Context Protocol (MCP) parent module
  - `spring-ai-mcp-filesystem` - Filesystem-based MCP integration (STDIO transport)
  - `spring-ai-mcp-weather` - Weather query MCP integration (SSE transport)
    - `spring-ai-mcp-weather-server` - Weather MCP server (port 8181)
    - `spring-ai-mcp-weather-client` - Weather MCP client with Ollama integration (port 8300)

### Architecture Patterns

**DDD Structure:**
```
src/main/java/
├── application/     # Use case orchestration, DTOs
├── domain/          # Entities, value objects, repositories, domain services
│   └── rag/         # RAG (Retrieval-Augmented Generation) specific
├── infrastructure/  # JPA repositories, config, external adapters
└── interfaces/      # Controllers, VO conversion
```

### Key Configuration

- **Java 17** required
- **Spring Boot**: 3.5.5
- **Spring AI**: 1.1.3 (configured via BOM in root pom)
- **MapStruct**: 1.6.3 (for DTO mapping)
- **Lombok**: Enabled with MapStruct binding

### External Dependencies

- **Ollama**: Required for local LLM (default port 11434)
  - Chat models: `deepseek-r1:8b`, `qwen3:8b`
  - Embedding models: `nomic-embed-text` (768 dim), `bge-m3` (1024 dim)
- **Elasticsearch**: Required for vector store (port 9200)
- **ChromaDB**: Alternative vector store (port 8001)

### Docker Setup

```bash
# Ollama (CPU or Nvidia GPU)
docker run --name ollama -d -v /data/ollama:/root/.ollama -p 11434:11434 ollama/ollama

# AMD GPU
docker pull ollama/ollama:rocm

# ChromaDB (optional)
docker run -d --name chromadb -p 8001:8000 -v /data/chroma:/chroma/chroma -e IS_PERSISTENT=TRUE chromadb/chroma

# Elasticsearch (for vector store)
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" elasticsearch:8.x
```

### Module Ports

| Module | Port | Description |
|--------|------|-------------|
| spring-ai-chat | 8100 | Chat API with streaming support |
| spring-ai-vector | 8200 | Vector store and RAG API |
| spring-ai-mcp-weather-server | 8181 | MCP Weather Server (SSE) |
| spring-ai-mcp-weather-client | 8300 | MCP Weather Client with Ollama |

### API Endpoints

**spring-ai-chat**
```bash
# Streaming chat
GET /chat/stream-query?query=你好

# Non-streaming chat
GET /chat/query?query=你好
```

**spring-ai-vector**
```bash
# Text embedding
GET /vector/text/v1/embedding?text=hello

# Store text
GET /vector/text/v1/store?text=hello

# Auto store text
GET /vector/text/v1/auto/store?text=hello
```

### Vector Store Configuration

Elasticsearch vector store uses these key parameters:
- `index-name`: Index name for vectors
- `dimensions`: Vector dimension (768 for nomic-embed-text, 1024 for bge-m3)
- `similarity`: `cosine`, `l2_norm`, or `dot_product`
- `embedding-field-name`: Custom field name for embedding vector

---

## MCP (Model Context Protocol) 配置

### MCP 依赖选择

| 传输类型 | 依赖 | 适用场景 |
|---------|------|---------|
| **STDIO** | `spring-ai-starter-mcp-server` | 命令行工具、桌面应用（如 Cherry Studio） |
| **WebMvc (SSE)** | `spring-ai-starter-mcp-server-webmvc` | 推荐：Spring WebMvc 项目 |
| **WebFlux (SSE)** | `spring-ai-starter-mcp-server-webflux` | 响应式项目 |

**注意**：当项目中同时存在 `DispatcherServlet` 和 `DispatcherHandler` 时，Spring Boot 优先使用 `DispatcherServlet`，因此使用 `spring-boot-starter-web` 时应选择 `webmvc` 依赖。

### MCP Server 配置属性

```yaml
spring:
  ai:
    mcp:
      server:
        # 基本信息
        enabled: true               # 启用/禁用
        name: weather-mcp-server    # 服务器名称
        version: 1.0.0              # 服务器版本
        type: SYNC                  # SYNC 或 ASYNC
        instructions: "服务器描述"   # 客户端交互指南
        stdio: false                # true=STDIO 传输，false=HTTP/SSE

        # 能力配置
        capabilities:
          tool: true                # 工具调用
          resource: true            # 资源访问
          prompt: true              # 提示词
          completion: true          # 自动完成

        # SSE 传输配置
        sse-endpoint: /sse                    # SSE 连接端点
        sse-message-endpoint: /mcp/message    # 消息发送端点
        base-url: /api/v1                     # 可选的 URL 前缀
        keep-alive-interval: 30s              # 心跳间隔

        # 变更通知
        tool-change-notification: true
        resource-change-notification: true
        prompt-change-notification: true

        # 请求超时
        request-timeout: 20s
```

### MCP 工具注册

**方式 1：@Tool 注解（推荐）**
```java
@Service
public class WeatherService {
    @Tool(description = "根据城市名获取天气信息")
    public String getWeather(String cityName) {
        // ...
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

Spring AI 会自动检测并注册：
- 单独的 `ToolCallback` Bean
- `ToolCallbackProvider` Bean
- `List<ToolCallback>` Bean

工具按名称去重，首次出现的工具会被使用。

### MCP 高级功能

**Logging（日志通知）**
```java
exchange.loggingNotification(LoggingMessageNotification.builder()
    .level(LoggingLevel.INFO)
    .logger("weather-logger")
    .data("天气查询完成")
    .build());
```

**Progress（进度通知）**
```java
exchange.progressNotification(ProgressNotification.builder()
    .progressToken("task-123")
    .progress(0.5)
    .total(1.0)
    .message("处理中...")
    .build());
```

**Ping（连接检查）**
```java
exchange.ping();
```

### MCP 端点

| 模块 | 端口 | SSE 端点 | 消息端点 |
|------|------|---------|---------|
| spring-ai-mcp-weather-server | 8181 | `/sse` | `/mcp/message` |
| spring-ai-mcp-filesystem | STDIO | N/A | N/A |

---

## Documentation Standards

- **README.md / CLAUDE.md**: 使用简单表格格式，不要用目录树结构或 Project 接口图
- 文档结构应包含：项目概述、安装说明、可用命令

## Git Workflow

- **Commit message**: 使用 gitmoji shortcode 格式（如 `:sparkles:`），不要用 unicode emoji 字符
- 提交前检查敏感文件（.env、credentials 等）
- 使用 `git add <具体文件>` 避免意外提交敏感数据

## File Operations

- 复制/移动文件后，验证操作是否成功再继续
- 批量文件变更前，先展示摘要计划并等待用户确认

## Interaction Preferences

- 执行多个工具调用或文件变更前，先展示摘要计划并等待用户确认
- 长时间运行的操作（编译、测试、安装）优先使用后台模式或 Headless Mode

## Long-running Operations

- 编译、测试、安装等操作，优先使用后台任务或 Headless Mode
- 操作完成后主动通知用户
