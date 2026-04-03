# CLAUDE.md

本文档为 Claude Code (claude.ai/code) 在本项目中工作时提供指导。

## 构建与测试命令

```bash
# 构建所有模块
mvn clean package

# 构建并跳过测试
mvn clean package -Dmaven.test.skip=true

# 运行单个模块
cd <module-name> && mvn spring-boot:run
```

## 项目结构

这是一个多模块 Maven 项目，包含 Spring AI 示例和集成。所有模块均使用 Spring Boot 3.5.5 和 Spring AI 2.0.0-M2。

### 模块组织

**核心模块：**
- `spring-ai-common` - AI 模块共享工具（Jackson、Lombok）
- `spring-ai-chat` - 与 Ollama 的 LLM 聊天集成（流式和非流式）
- `spring-ai-vector` - 向量存储（Elasticsearch）、PDF 文档读取器、RAG
- `spring-ai-openmanus` - OpenManus 集成
- `spring-ai-mcp` - 模型上下文协议（MCP）父模块
  - `spring-ai-mcp-filesystem` - 基于文件系统的 MCP 集成（STDIO 传输）
  - `spring-ai-mcp-weather` - 天气查询 MCP 集成（SSE 传输）
    - `spring-ai-mcp-weather-server` - 天气 MCP 服务器（端口 8181）
    - `spring-ai-mcp-weather-client` - 带 Ollama 集成的天气 MCP 客户端（端口 8300）

### 架构模式

**DDD 结构：**
```
src/main/java/
├── application/     # 用例编排、DTO
├── domain/          # 实体、值对象、仓储、领域服务
│   └── rag/         # RAG（检索增强生成）相关
├── infrastructure/  # JPA 仓储、配置、外部适配器
└── interfaces/      # 控制器、VO 转换
```

### 关键配置

- **Java 17** 必需
- **Spring Boot**: 3.5.5
- **Spring AI**: 1.1.3（在根 pom 中通过 BOM 配置）
- **MapStruct**: 1.6.3（用于 DTO 映射）
- **Lombok**：启用并与 MapStruct 绑定

### 外部依赖

- **Ollama**：本地 LLM 必需（默认端口 11434）
  - 聊天模型：`deepseek-r1:8b`、`qwen3:8b`
  - 嵌入模型：`nomic-embed-text`（768 维）、`bge-m3`（1024 维）
- **Elasticsearch**：向量存储必需（端口 9200）
- **ChromaDB**：可选向量存储（端口 8001）

### Docker 配置

```bash
# Ollama（CPU 或 Nvidia GPU）
docker run --name ollama -d -v /data/ollama:/root/.ollama -p 11434:11434 ollama/ollama

# AMD GPU
docker pull ollama/ollama:rocm

# ChromaDB（可选）
docker run -d --name chromadb -p 8001:8000 -v /data/chroma:/chroma/chroma -e IS_PERSISTENT=TRUE chromadb/chroma

# Elasticsearch（用于向量存储）
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" elasticsearch:8.x
```

### 模块端口

| 模块 | 端口 | 说明 |
|--------|------|-------------|
| spring-ai-chat | 8100 | 支持流式的聊天 API |
| spring-ai-vector | 8200 | 向量存储和 RAG API |
| spring-ai-mcp-weather-server | 8181 | MCP 天气服务器（SSE） |
| spring-ai-mcp-weather-client | 8300 | 带 Ollama 集成的 MCP 天气客户端 |

### API 端点

**spring-ai-chat**
```bash
# 流式聊天
GET /chat/stream-query?query=你好

# 非流式聊天
GET /chat/query?query=你好
```

**spring-ai-vector**
```bash
# 文本嵌入
GET /vector/text/v1/embedding?text=hello

# 存储文本
GET /vector/text/v1/store?text=hello

# 自动存储文本
GET /vector/text/v1/auto/store?text=hello
```

### 向量存储配置

Elasticsearch 向量存储使用以下关键参数：
- `index-name`：向量索引名称
- `dimensions`：向量维度（nomic-embed-text 为 768，bge-m3 为 1024）
- `similarity`：`cosine`、`l2_norm` 或 `dot_product`
- `embedding-field-name`：嵌入向量的自定义字段名

---

## MCP（模型上下文协议）配置

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

## MCP Client 配置

### MCP Client 依赖选择

| 依赖 | 传输类型 | 适用场景 |
|------|---------|---------|
| `spring-ai-starter-mcp-client` | STDIO + HttpClient (SSE/Streamable-HTTP) | 标准客户端 |
| `spring-ai-starter-mcp-client-webflux` | STDIO + WebFlux (SSE/Streamable-HTTP) | 推荐：响应式项目 |

**注意**：SYNC 和 ASYNC 客户端不能混用，所有客户端必须同为同步或异步。

### MCP Client 配置属性

#### 通用配置 (`spring.ai.mcp.client`)

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 启用/禁用客户端 | `true` |
| `name` | 客户端名称 | `spring-ai-mcp-client` |
| `version` | 客户端版本 | `1.0.0` |
| `request-timeout` | 请求超时时间 | `20s` |
| `type` | 客户端类型：SYNC 或 ASYNC | `SYNC` |
| `toolcallback.enabled` | 启用工具回调集成 | `true` |

#### SSE 传输配置 (`spring.ai.mcp.client.sse`)

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            weather-server:
              url: http://localhost:8181        # 基础 URL
              sse-endpoint: /sse                # SSE 端点（默认 /sse）
```

#### Streamable-HTTP 传输配置

```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            server1:
              url: http://localhost:8080
              endpoint: /mcp                    # HTTP 端点（默认 /mcp）
```

#### STDIO 传输配置

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          connections:
            filesystem:
              command: npx                      # Windows 需用 cmd.exe /c
              args: ["-y", "@modelcontextprotocol/server-filesystem", "/path"]
              env:
                API_KEY: your-key
```

**Windows STDIO 特殊配置**（npx/npm/node 是批处理文件，需要 cmd.exe 包装）：
```yaml
command: cmd.exe
args: ["/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"]
```

### 工具名称前缀生成器

当连接多个 MCP 服务器时，可能出现工具名称冲突。Spring AI 默认行为：
- 自动检测重复工具名并添加前缀（如 `alt_1_search`）
- 替换非字母数字字符为下划线（如 `my-tool` → `my_tool`）

**禁用前缀**（仅当确定无冲突时）：
```java
@Configuration
public class McpConfiguration {
    @Bean
    public McpToolNamePrefixGenerator mcpToolNamePrefixGenerator() {
        return McpToolNamePrefixGenerator.noPrefix();
    }
}
```

### 客户端自定义配置

通过 `McpSyncClientCustomizer` 或 `McpAsyncClientCustomizer` 自定义客户端：

```java
@Component
public class CustomMcpClientCustomizer implements McpSyncClientCustomizer {
    @Override
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {
        // 设置请求超时
        spec.requestTimeout(Duration.ofSeconds(30));

        // 设置工具变更通知
        spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {
            System.out.println("工具列表已更新：" + tools.size() + " 个工具");
        });

        // 设置日志消费者
        spec.loggingConsumer((McpSchema.LoggingMessageNotification log) -> {
            System.out.println("日志：" + log.level() + " - " + log.data());
        });

        // 设置进度消费者
        spec.progressConsumer((ProgressNotification progress) -> {
            System.out.println("进度：" + progress.progress() * 100 + "%");
        });
    }
}
```

**可用的自定义项**：
- `requestTimeout()` - 请求超时配置
- `toolsChangeConsumer()` - 工具变更通知
- `resourcesChangeConsumer()` - 资源变更通知
- `promptsChangeConsumer()` - 提示词变更通知
- `loggingConsumer()` - 日志消息消费
- `progressConsumer()` - 进度通知消费
- `sampling()` - 自定义采样处理
- `elicitation()` - 自定义信息收集处理

### 工具过滤

通过 `McpToolFilter` 接口过滤工具：

```java
@Component
public class CustomMcpToolFilter implements McpToolFilter {
    @Override
    public boolean test(McpConnectionInfo connectionInfo, McpSchema.Tool tool) {
        // 排除特定客户端的工具
        if (connectionInfo.clientInfo().name().equals("restricted-client")) {
            return false;
        }
        // 只包含特定前缀的工具
        return tool.name().startsWith("allowed_");
    }
}
```

**注意**：应用中只应定义一个 `McpToolFilter` Bean。

### MCP Client 注解

使用注解处理 MCP 客户端事件：

| 注解 | 说明 |
|------|------|
| `@McpLogging` | 处理服务器日志消息 |
| `@McpSampling` | 处理 LLM 采样请求 |
| `@McpElicitation` | 处理信息收集请求 |
| `@McpProgress` | 处理进度通知 |
| `@McpToolListChanged` | 处理工具列表变更 |
| `@McpResourceListChanged` | 处理资源列表变更 |
| `@McpPromptListChanged` | 处理提示词列表变更 |

**使用示例**：
```java
@Component
public class McpClientHandlers {

    @McpLogging(clients = "weather-server")
    public void handleLogging(LoggingMessageNotification notification) {
        System.out.println("日志：" + notification.level() + " - " + notification.data());
    }

    @McpToolListChanged(clients = "weather-server")
    public void handleToolListChanged(List<McpSchema.Tool> tools) {
        System.out.println("工具列表更新：" + tools.size() + " 个工具");
    }

    @McpProgress(clients = "weather-server")
    public void handleProgress(ProgressNotification notification) {
        System.out.printf("进度：%.2f%% - %s%n",
            notification.progress() * 100, notification.message());
    }
}
```

注解支持指定客户端：`clients = "server-name"`

---

## 文档规范

- **README.md / CLAUDE.md**：使用简单表格格式，不要用目录树结构或 Project 接口图
- 文档结构应包含：项目概述、安装说明、可用命令

## Git 工作流

- **Commit message**：使用 gitmoji shortcode 格式（如 `:sparkles:`），不要用 unicode emoji 字符
- 提交前检查敏感文件（.env、credentials 等）
- 使用 `git add <具体文件>` 避免意外提交敏感数据

## 文件操作

- 复制/移动文件后，验证操作是否成功再继续
- 批量文件变更前，先展示摘要计划并等待用户确认

## 交互偏好

- 执行多个工具调用或文件变更前，先展示摘要计划并等待用户确认
- 长时间运行的操作（编译、测试、安装）优先使用后台模式或 Headless Mode

## 长时间运行的操作

- 编译、测试、安装等操作，优先使用后台任务或 Headless Mode
- 操作完成后主动通知用户

## 代码规范

### 文件头注释

创建新的Java文件时，必须添加以下头注释（IDE会自动替换日期和时间）：

```java
/**
 * @author: kiturone
 * @date: ${DATE} ${TIME}
 * @description: 文件描述内容
 */
```