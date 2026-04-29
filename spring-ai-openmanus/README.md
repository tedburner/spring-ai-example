# Spring AI OpenManus

OpenManus 集成模块，基于 Spring AI 1.1.5 和 Spring Boot 3.5.5。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17 |
| Spring Boot | 3.5.5 |
| Spring AI | 1.1.5 |
| Ollama | 本地大模型 |

## 快速开始

```bash
cd spring-ai-openmanus
mvn spring-boot:run
```

## 配置

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: deepseek-r1:8b
```
