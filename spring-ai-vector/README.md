## 基于DDD项目框架
```bash
src/main/java/com/ai/knowledge/vector
├── application/           # 应用层：用例编排、事务管理
│   ├── service/
│   │   ├── VectorApplicationService.java    # 向量化用例
│   │   └── impl/DocumentRagApplicationServiceImpl.java  # RAG 用例
│   └── dto/               # 应用层 DTO
├── domain/                # 领域层：实体、仓储接口、领域服务
│   ├── entity/
│   │   ├── DocumentRagResultDTO.java        # RAG 结果
│   │   └── VectorStoreResultDTO.java        # 存储结果
│   ├── repository/
│   │   └── VectorStoreRepository.java       # 仓储接口
│   ├── rag/
│   │   ├── answer/                          # 答案生成领域服务
│   │   ├── metadata/                        # 元数据管理领域服务
│   │   └── query/                           # 查询重写领域服务
│   └── vector/service/
│       └── DocumentRagService.java          # RAG 领域服务
├── infrastructure/        # 基础设施层：仓储实现
│   └── repository/
│       └── impl/
│           └── VectorEsStoreRepositoryImpl.java  # ES 向量仓储
└── interfaces/            # 接口层：Controller、VO
    ├── controller/
    │   └── DocumentRagController.java         # RAG REST 控制器
    └── vo/vector/
        └── DocumentRagResultVO.java           # RAG 响应 VO
```
## 向量模型
向量化模型不需要启动，只需要拉取模型即可。
### nomic-embed-text
向量纬度：768

### bge-m3
向量纬度：1024

## 向量数据库
### chromadb
```bash
docker run -d --name chromadb -p 8001:8000 -v D:\Document\docker\chroma:/chroma/chroma -e IS_PERSISTENT=TRUE -e ANONYMIZED_TELEMETRY=TRUE chromadb/chroma
```

### elasticsearch

## spring ai
### Spring elasticsearch vector store 固定的参数：
该实体的主要参数是下面几个：
- id：文档的唯一标识符，用于检索和管理文档。
- content：文档的内容，通常是文本、图片或其他形式的媒体。
- metadata：文档的元数据，用于存储额外的信息，如文档的创建时间、作者、标签等。
- contentType：文档的内容类型，用于指定文档的类型，如文本、图片、音频等。
- embedding：可在配置文件中自定义向量化字段，如果不配置，则默认使用embedding字段作为向量化存储字段。