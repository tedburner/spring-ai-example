# Spring AI Chat 模块

基于 Spring AI 1.1.5 的聊天模块，支持 Ollama 本地大模型、Agent 工具调用、Tavily 网络搜索、Prompt Engineering Patterns 和 Agentic Workflows 等功能。

## 快速开始

### 1. 启动 Ollama

```bash
# CPU 或 Nvidia GPU 
docker pull ollama/ollama

# AMD GPU
docker pull ollama/ollama:rocm

# 启动 Ollama
docker run --name ollama -d -v D:\Document\ollama:/root/.ollama -p 11434:11434 ollama/ollama

# 拉取模型
ollama pull deepseek-r1:8b
ollama pull qwen3:8b
```

### 2. 配置 Tavily API Key（可选）

获取 Tavily API Key：https://tavily.com/

在 `application.yml` 或环境变量中设置：
```yaml
tavily:
  api:
    key: your-tavily-api-key
```

### 3. 启动应用

```bash
cd spring-ai-chat
mvn spring-boot:run
```

应用启动在端口 `8100`。

---

## API 端点

### 传统聊天接口

| 端点 | 说明 |
|------|------|
| `GET /chat/query?query=你好` | 非流式对话 |
| `GET /chat/stream-query?query=你好` | 流式对话（SSE） |
| `GET /chat/memory/query?query=你好&sessionId=1` | 带记忆的非流式对话 |
| `GET /chat/memory/stream-query?query=你好&sessionId=1` | 带记忆的流式对话 |

### Agent 接口（新增）

| 端点 | 说明 |
|------|------|
| `POST /agent/chat` | 非流式 Agent 对话（支持工具调用） |
| `POST /agent/stream` | 流式 Agent 对话（SSE） |
| `POST /agent/chat/memory` | 带会话记忆的 Agent 对话 |
| `POST /agent/tavily/search` | Tavily 网络搜索 |
| `GET /agent/tavily/search?query=AI news` | 简化的搜索接口 |

### Agent 工作流

| 端点 | 说明 |
|------|------|
| `POST /agent/workflow/chain` | 链式工作流（顺序 LLM 管道） |
| `POST /agent/workflow/parallel/sectioning` | 并行分段（并发子任务） |
| `POST /agent/workflow/parallel/voting` | 并行投票（多调用取共识） |
| `POST /agent/workflow/routing` | 路由分类（JSON 分类 + 路由） |

### Prompt Engineering Patterns

| 端点 | 说明 |
|------|------|
| `POST /pattern/invoke` | 统一 Pattern 调用 |
| `POST /pattern/zero-shot` | Zero-shot Prompting |
| `POST /pattern/few-shot` | Few-shot Prompting |
| `POST /pattern/system` | System Prompting |
| `POST /pattern/role` | Role Prompting |
| `POST /pattern/contextual` | Contextual Prompting |
| `POST /pattern/cot` | Chain of Thought |
| `POST /pattern/code/write` | 代码生成 |
| `POST /pattern/code/explain` | 代码解释 |
| `POST /pattern/code/translate` | 代码翻译 |
| `POST /pattern/step-back` | Step-back Prompting |
| `POST /pattern/self-consistency` | 自一致性 |
| `POST /pattern/tree-of-thoughts` | 思维树（Tree of Thoughts） |
| `POST /pattern/auto-prompt` | 自动 Prompt 工程 |
| `GET /pattern/types` | 获取所有 Pattern 类型 |

### 结构化输出

| 端点 | 说明 |
|------|------|
| `POST /structured/weather` | 天气数据结构化输出 |
| `POST /structured/analyze` | 文本分析结构化输出 |

---

## Agent 功能

### 可用工具

Agent 支持以下工具调用：

| 工具 | 说明 | 示例 |
|------|------|------|
| `getCurrentTime` | 获取当前时间 | "现在几点了？" |
| `getCurrentDate` | 获取当前日期 | "今天是什么日子？" |
| `calculateMath` | 计算数学表达式 | "计算 (10-5)*2" |
| `getWeather` | 获取天气信息（模拟） | "北京天气怎么样？" |
| `generateRandomNumber` | 生成随机数 | "生成 1-100 的随机数" |
| `calculateBMI` | 计算 BMI 指数 | "身高 1.75m，体重 70kg 的 BMI" |
| `reverseString` | 反转字符串 | "反转 hello" |
| `getStringLength` | 计算字符串长度 | "hello 有多长？" |
| `tavilySearch` | Tavily 网络搜索 | "2025 年 AI 最新进展" |

### 请求示例

#### 1. 非流式 Agent 对话

```bash
curl -X POST http://localhost:8100/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？", "enableTools": true}'
```

#### 2. 流式 Agent 对话

```bash
curl -X POST http://localhost:8100/agent/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "帮我计算 (10+5)*2 等于多少"}'
```

#### 3. Tavily 网络搜索

```bash
curl -X POST http://localhost:8100/agent/tavily/search \
  -H "Content-Type: application/json" \
  -d '{"query": "Spring AI 1.1.5 new features", "maxResults": 5}'
```

---

## 前端测试页面

启动应用后，访问以下 URL 打开测试界面：

```
http://localhost:8100/static/agent.html
```

测试页面支持三种模式：
- **流式对话**：实时显示 AI 回复
- **普通对话**：完整回复后显示
- **网络搜索**：使用 Tavily 搜索最新信息

---

## 配置说明

### application.yml

```yaml
server:
  port: 8100

spring:
  application:
    name: spring-ai-chat
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: deepseek-r1:8b
    chat:
      memory:
        enabled: true
        type: in_memory
        ttl: 30m
        capacity: 10

# Tavily API 配置
tavily:
  api:
    key: ${TAVILY_API_KEY:}
```

### 环境变量

```bash
# Windows PowerShell
$env:TAVILY_API_KEY="your-api-key"

# Linux/Mac
export TAVILY_API_KEY="your-api-key"
```

---

## 项目结构

```
spring-ai-chat/
├── src/main/java/com/ai/chat/
│   ├── application/
│   │   ├── config/
│   │   │   ├── AgentConfig.java          # Agent 配置
│   │   │   ├── ChatMemoryConfig.java
│   │   │   ├── PromptPatternOptions.java # Prompt Pattern 配置
│   │   │   └── PromptTemplateConfig.java
│   │   ├── service/
│   │   │   ├── AgentService.java         # Agent 核心服务
│   │   │   ├── ToolService.java          # 工具服务
│   │   │   ├── WorkflowService.java      # 工作流编排
│   │   │   ├── PromptPatternService.java # Prompt Pattern 服务
│   │   │   └── ...
│   │   ├── advisor/
│   │   │   ├── AugmentedToolCallback.java      # 工具参数增强
│   │   │   └── AugmentedToolCallbackProvider.java
│   │   └── interceptor/                  # 拦截器（限流、验证等）
│   │
│   ├── domain/
│   │   └── entity/
│   │       ├── SearchResult.java         # 搜索结果实体
│   │       ├── TavilySearchResponse.java # Tavily 响应实体
│   │       ├── WeatherData.java
│   │       └── TextAnalysis.java
│   │
│   ├── interfaces/
│   │   ├── controller/
│   │   │   ├── AgentController.java      # Agent REST 控制器
│   │   │   ├── ChatController.java
│   │   │   └── PromptPatternController.java  # Prompt Pattern 端点
│   │   └── dto/
│   │       ├── AgentChatRequest.java     # Agent 请求 DTO
│   │       ├── TavilySearchRequest.java  # 搜索请求 DTO
│   │       ├── PromptPatternRequest.java # Prompt Pattern 请求
│   │       ├── PromptPatternResponse.java # Prompt Pattern 响应
│   │       └── service/dto/              # 工作流 DTO
│   │           ├── ChainResult.java
│   │           ├── ParallelResult.java
│   │           ├── RouteClassification.java
│   │           └── RoutingDecision.java
│   │
│   └── SpringAiChatApplication.java
│
└── src/main/resources/
    ├── application.yml
    └── static/
        └── agent.html                    # 前端测试页面
```

---

## 技术栈

- **Spring Boot**: 3.5.5
- **Spring AI**: 1.1.5
- **Ollama**: 本地大模型运行环境
- **WebFlux**: 响应式 Web 框架
- **Lombok**: 简化 Java 代码
- **MapStruct**: DTO 映射

---

## 注意事项

1. **Tavily 搜索** 需要有效的 API Key，否则搜索功能将不可用
2. **工具调用** 需要 LLM 模型支持 function calling（deepseek-r1:8b 支持）
3. **流式响应** 使用 SSE（Server-Sent Events），浏览器需支持
