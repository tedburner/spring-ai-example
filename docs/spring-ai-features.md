# Spring AI 特性清单

来源：`F:\Project\GitHub\spring-ai-examples` 官方示例项目
分析日期：2026-04-28

## 当前项目已有功能

- ChatMemory 会话记忆
- Observability 可观测性（Actuator + Prometheus）
- Structured Output 结构化输出
- Prompt Template 提示词模板
- ToolService 工具服务
- Advisor 体系（ValidationAdvisor、LoggingAdvisor、RateLimitAdvisor）
- MCP Server/Client（天气查询、文件系统）
- RAG（PDF 文档阅读器、向量存储）
- Agent 模块（工具调用 + Tavily 网络搜索）

---

## 第一优先级（强烈推荐，立即引入）

### 1. Prompt Engineering Patterns

- **示例位置**：`prompt-engineering/prompt-engineering-patterns/`
- **说明**：包含 13 种经典提示词模式：
  - Zero-shot、One-shot/Few-shot
  - System prompting、Role prompting、Contextual prompting
  - Chain of Thought、Self-consistency、Tree of Thoughts
  - Step-back prompting
  - 代码编写/解释/翻译
- **优势**：零依赖，直接复用，可立刻提升所有现有 LLM 接口输出质量
- **引入方式**：复制 Prompt 模板到 `PromptTemplateManager`，新增对应的模板名称和实现

### 2. Tool Argument Augmenter（工具参数增强）

- **示例位置**：`advisors/tool-argument-augmenter-demo/`
- **说明**：通过 `AugmentedToolCallbackProvider` 为工具调用增加额外思考参数（innerThought、confidence、memoryNotes），让 LLM 在调用工具前输出推理过程
- **优势**：提升工具调用的可观测性和调试能力，能看到 LLM "为什么"调用某个工具
- **引入方式**：新建 `ToolArgumentAugmenterAdvisor`，结合现有 Advisor 体系

### 3. Chain Workflow（链式工作流）

- **示例位置**：`agentic-patterns/chain-workflow/`
- **说明**：将复杂任务分解为顺序的 LLM 调用步骤，每步输出作为下一步输入。示例：提取数值 -> 标准化格式 -> 排序 -> 格式化为 Markdown 表格
- **优势**：适合需要多步处理的场景（数据清洗管道、多步内容生成），实现清晰
- **引入方式**：新建 `ChainWorkflow` 服务，提供编排 API

### 4. Parallelization Workflow（并行工作流）

- **示例位置**：`agentic-patterns/parallelization-workflow/`
- **说明**：基于线程池的并发 LLM 调用，支持两种模式：
  - **Sectioning**：拆分独立子任务并行执行
  - **Voting**：同一任务多次执行取共识
- **优势**：显著提升批量处理吞吐量
- **引入方式**：新建 `ParallelizationWorkflow` 服务

---

## 第二优先级（按需引入）

### 5. MCP Annotations（MCP 注解驱动开发）

- **示例位置**：`model-context-protocol/mcp-annotations/`
- **说明**：使用 `@McpTool`、`@McpToolParam`、`@McpResource` 注解声明式定义 MCP 工具和资源，支持 completions（自动补全参数值）和 prompts（模板化提示）
- **优势**：比手动构建 MCP Server 更简洁，减少样板代码
- **引入方式**：改造现有 MCP Server 模块使用注解方式

### 6. MCP Dynamic Tool Update（MCP 动态工具更新）

- **示例位置**：`model-context-protocol/dynamic-tool-update/`
- **说明**：MCP Server 运行时动态增删工具：通过 HTTP endpoint 接收信号，调用 `mcpSyncServer.addTool()` 热更新工具列表
- **优势**：支持插件化架构和热更新，无需重启
- **引入方式**：在 MCP Server 模块新增动态注册/注销端点

### 7. Routing Workflow（路由工作流）

- **示例位置**：`agentic-patterns/routing-workflow/`
- **说明**：使用结构化输出将输入分类到不同路由，每个路由对应 specialized prompt。含 JSON 格式的 reasoning + selection 决策
- **优势**：适合构建智能客服/工单分发系统，当前项目缺少输入分类路由能力
- **引入方式**：新建 `RoutingWorkflow` 服务

---

## 第三优先级（长期规划）

### 8. Orchestrator-Workers（编排者-工作者模式）

- **示例位置**：`agentic-patterns/orchestrator-workers/`
- **说明**：中央 LLM 动态拆解任务为子任务，分发 Worker LLMs 并行执行，最后合成结果
- **优势**：适合复杂不可预测的任务场景
- **注意**：需要评估性能开销（多次 LLM 调用）

### 9. MCP Sampling（MCP 采样/多模型代理）

- **示例位置**：`model-context-protocol/sampling/`
- **说明**：MCP Server 端工具通过 `McpToolUtils.getMcpExchange()` 获取采样能力，向 Client 发送 `CreateMessageRequest` 调用不同 LLM，实现多模型协作
- **优势**：允许一个模型处理工具逻辑，另一个模型做创意生成
- **注意**：高级特性，需要多模型环境

### 10. Integration Testing Framework（集成测试框架）

- **示例位置**：`integration-testing/`
- **说明**：完整的集成测试基础设施：JBang 脚本 + Python 编排器 + JSON 配置 + 集中日志管理 + 端口清理
- **优势**：18 个 JBang 脚本已集中化，零代码重复
- **引入方式**：参考其架构搭建本地测试环境

---

## 不推荐引入（已有重叠或场景不符）

| 特性 | 原因 |
|------|------|
| Reflection Agent | 与现有 Advisor 体系功能重叠，Advisor 方式更灵活 |
| SelfRefineEvaluationAdvisor | 已在 Advisor 体系中实现 |
| Brave Web Search MCP | 已有 Tavily 网络搜索 |
| MCP SQLite Database | 当前项目使用 Elasticsearch，场景不同 |
| Document Forge (Office 生成) | 依赖 Anthropic 专用 SDK，需评估是否适配 Ollama |
