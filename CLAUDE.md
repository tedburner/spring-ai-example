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

This is a multi-module Maven project containing Spring AI examples and integrations. Spring Boot 3.5.5 and Spring AI 1.0.1 are used across all modules.

### Module Organization

**Core Modules:**
- `spring-ai-common` - Shared utilities for AI modules (Jackson, Lombok)
- `spring-ai-chat` - LLM chat integration with Ollama and vector store
- `spring-ai-vector` - Vector store with Elasticsearch, PDF document reader
- `spring-ai-openmanus` - OpenManus integration
- `spring-ai-mcp` - Model Context Protocol (MCP) parent module
  - `spring-ai-mcp-stdio` - STDIO-based MCP transport
    - `spring-ai-mcp-stdio-client` - MCP client
    - `spring-ai-mcp-stdio-server` - MCP server with Ollama
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
- **Spring AI**: 1.0.1 (configured via BOM in root pom)
- **MapStruct**: 1.6.3 (for DTO mapping)
- **Lombok**: Enabled with MapStruct binding

### External Dependencies

- **Ollama**: Required for local LLM (default port 11434)
  - Models: deepseek-r1:8b, qwen3:8b
- **Elasticsearch**: Required for vector store
- **ChromaDB**: Alternative vector store (port 8001)

### Docker Setup

```bash
# Ollama (CPU or Nvidia GPU)
docker run --name ollama -d -v /data/ollama:/root/.ollama -p 11434:11434 ollama/ollama

# ChromaDB (optional)
docker run -d --name chromadb -p 8001:8000 -e IS_PERSISTENT=TRUE chromadb/chroma

# Elasticsearch (for vector store)
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" elasticsearch:8.x
```
