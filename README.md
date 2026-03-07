# Spring AI Project

基于 Spring AI 框架的大模型应用项目，支持快速接入本地大模型（Ollama）和云端大模型。

## 项目结构

```
spring-ai-project/
├── spring-ai-common                        # 通用工具模块
├── spring-ai-chat                          # AI 聊天服务
├── spring-ai-vector                        # 向量化服务（RAG）
├── spring-ai-openmanus                     # OpenManus 集成
└── spring-ai-mcp                           # MCP (Model Context Protocol)
    ├── spring-ai-mcp-stdio                 # STDIO 传输方式
    │   ├── spring-ai-mcp-stdio-client      # 客户端
    │   └── spring-ai-mcp-stdio-server      # 服务端
    └── spring-ai-mcp-streamable            # Streamable HTTP 传输方式
        ├── spring-ai-mcp-streamable-client # 客户端
        └── spring-ai-mcp-streamable-server # 服务端
```

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.5.5 |
| Spring AI | 1.0.1 |
| MapStruct | 1.6.3 |
| Lombok | ✓ |

## 快速开始

### 1. 安装 Ollama

```bash
# Docker 方式安装（CPU 或 Nvidia GPU）
docker pull ollama/ollama
docker run --name ollama -d -v /data/ollama:/root/.ollama -p 11434:11434 ollama/ollama

# AMD GPU
docker pull ollama/ollama:rocm
```

### 2. 拉取模型

```bash
# 查看可用模型
docker exec -it ollama ollama list

# 拉取并运行模型
ollama pull deepseek-r1:8b
ollama run deepseek-r1:8b

# 或使用通义千问
ollama pull qwen3:8b
ollama run qwen3:8b
```

### 3. 向量化模型

向量化模型只需拉取，无需启动服务：

```bash
# nomic-embed-text (768 维)
ollama pull nomic-embed-text

# bge-m3 (1024 维)
ollama pull bge-m3
```

### 4. 向量数据库

**ChromaDB**
```bash
docker run -d --name chromadb \
  -p 8001:8000 \
  -v /data/chroma:/chroma/chroma \
  -e IS_PERSISTENT=TRUE \
  -e ANONYMIZED_TELEMETRY=TRUE \
  chromadb/chroma
```

**Elasticsearch**
```bash
docker run -d --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  elasticsearch:8.x
```

### 5. 运行项目

```bash
# 编译并运行聊天服务
cd spring-ai-chat
mvn spring-boot:run

# 编译并运行向量化服务
cd spring-ai-vector
mvn spring-boot:run
```

## 模块说明

### spring-ai-chat

AI 聊天服务，支持流式和非流式对话。

**API 接口**

```bash
# 流式对话
GET /chat/stream-query?query=你好

# 非流式对话
GET /chat/query?query=你好
```

### spring-ai-vector

向量化服务，实现 RAG（检索增强生成）功能。

**支持特性**
- PDF 文档读取
- 向量存储（Elasticsearch/ChromaDB）
- 语义检索

**Spring AI Vector Store 核心参数**
- `id` - 文档唯一标识
- `content` - 文档内容
- `metadata` - 元数据（创建时间、作者、标签等）
- `contentType` - 内容类型（文本/图片/音频等）
- `embedding` - 向量数据（可自定义字段名）

### spring-ai-mcp

Model Context Protocol (MCP) 实现，支持两种传输方式：

| 传输方式 | 模块 | 适用场景 |
|---------|------|---------|
| STDIO | spring-ai-mcp-stdio | 本地进程间通信 |
| Streamable HTTP | spring-ai-mcp-streamable | 远程网络通信 |

## 架构设计

采用 DDD（领域驱动设计）分层架构：

```
src/main/java/
├── application/        # 应用层：用例编排、事务管理、事件发布
├── domain/             # 领域层：实体、值对象、聚合根、仓储接口
│   └── rag/            # RAG 相关领域逻辑
├── infrastructure/     # 基础设施层：Repository 实现、第三方适配器
└── interfaces/         # 接口层：Controller、DTO/VO 转换
```

## 开发指南

### 添加新模型

在 `application.yml` 中配置：

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: qwen3:8b
```

### 自定义向量化字段

```yaml
spring:
  ai:
    vectorstore:
      elasticsearch:
        index-name: my-vectors
        embedding-dimension: 1024
        vector-dimension-key: embedding  # 自定义向量字段名
```

## 参考资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Ollama](https://ollama.ai/)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [ChromaDB](https://docs.trychroma.com/)
