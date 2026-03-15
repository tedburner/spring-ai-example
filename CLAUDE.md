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
  - `spring-ai-mcp-filesystem` - Filesystem-based MCP integration
  - `spring-ai-mcp-streamable` - Streamable HTTP-based MCP transport
    - `spring-ai-mcp-streamable-client` - Streamable MCP client
    - `spring-ai-mcp-streamable-server` - Streamable MCP server

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
- **Spring AI**: 2.0.0-M2 (configured via BOM in root pom)
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
