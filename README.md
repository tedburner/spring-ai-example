# Spring AI Project

基于 Spring AI 框架的大模型应用项目，支持快速接入本地大模型（Ollama）和云端大模型。

## 项目结构

```
spring-ai-project/
├── spring-ai-common                        # 通用工具模块
├── spring-ai-chat                          # AI 聊天服务（端口 8100）
├── spring-ai-vector                        # 向量化服务 + RAG（端口 8200）
├── spring-ai-openmanus                     # OpenManus 集成
└── spring-ai-mcp                           # MCP (Model Context Protocol)
    ├── spring-ai-mcp-filesystem             # 文件系统 MCP 集成（STDIO 传输）
    └── spring-ai-mcp-weather                # 天气查询 MCP 集成（SSE 传输）
        ├── spring-ai-mcp-weather-server     # MCP 服务器（端口 8181）
        └── spring-ai-mcp-weather-client     # MCP 客户端（端口 8300）
```

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.5.5 |
| Spring AI | 1.1.5 |
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

AI 聊天服务，支持流式和非流式对话、Agent 工具调用、Prompt Engineering Patterns。

**API 接口**

```bash
# 传统聊天
GET /chat/query?query=你好
GET /chat/stream-query?query=你好
GET /chat/memory/query?query=你好&sessionId=1
GET /chat/memory/stream-query?query=你好&sessionId=1

# Agent 对话（支持工具调用）
POST /agent/chat              # 非流式
POST /agent/stream            # 流式 SSE
POST /agent/chat/memory       # 带记忆

# Agent 工作流
POST /agent/workflow/chain              # 链式工作流
POST /agent/workflow/parallel/sectioning # 并行分段
POST /agent/workflow/parallel/voting     # 并行投票
POST /agent/workflow/routing             # 路由分类

# Prompt Engineering Patterns
POST /pattern/invoke           # 统一调用
POST /pattern/zero-shot        # Zero-shot
POST /pattern/few-shot         # Few-shot
POST /pattern/system           # System prompting
POST /pattern/role             # Role prompting
POST /pattern/contextual       # Contextual prompting
POST /pattern/cot              # Chain of Thought
POST /pattern/code/write       # 代码生成
POST /pattern/code/explain     # 代码解释
POST /pattern/code/translate   # 代码翻译
POST /pattern/step-back        # Step-back prompting
POST /pattern/self-consistency # 自一致性
POST /pattern/tree-of-thoughts # 思维树
POST /pattern/auto-prompt      # 自动 Prompt 工程
GET  /pattern/types            # 获取所有 Pattern 类型

# Tavily 网络搜索
POST /agent/tavily/search      # POST 搜索
GET  /agent/tavily/search?query=xxx  # GET 搜索

# 结构化输出
POST /structured/weather       # 天气结构化
POST /structured/analyze       # 文本分析结构化
```

### spring-ai-vector

向量化服务，实现完整 RAG（检索增强生成）管道。

**支持特性**
- PDF 文档解析（PagePdfDocumentReader）
- TokenTextSplitter 自动分块
- 向量存储（Elasticsearch）
- 语义相似度检索
- RAG 问答（检索 + LLM 生成）
- 文档元数据管理与过滤

**API 接口**

```bash
# 向量化
GET /vector/text/v1/embedding?text=hello
GET /vector/text/v1/store?text=hello
GET /vector/text/v1/auto/store?text=hello

# RAG 文档解析
POST /document/rag/v1/parse      # 上传 PDF 并解析

# RAG 问答
POST /document/rag/v1/ask        # 检索 + 生成答案

# 向量存储
POST /vector/rag/v1/retrieve     # 语义检索
```

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
| STDIO | spring-ai-mcp-filesystem | 本地进程间通信（文件系统） |
| SSE | spring-ai-mcp-weather | 远程网络通信（天气查询） |

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
