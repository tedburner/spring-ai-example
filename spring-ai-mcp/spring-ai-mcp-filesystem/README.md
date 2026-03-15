# Spring AI MCP Filesystem

基于 Spring AI MCP 协议的本地文件读取服务器，允许 LLM 通过 MCP 协议安全地读取指定目录下的文件。

## 技术栈

- Java 17+
- Spring Boot 3.5.5
- Spring AI 2.0.0-M2

## 快速开始

### 1. 构建项目

```bash
cd spring-ai-mcp/spring-ai-mcp-filesystem
mvn clean package -DskipTests
```

构建成功后，在 `target/` 目录下生成可执行 JAR 文件。

### 2. 配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  main:
    web-application-type: none  # STDIO 模式需禁用 Web
    banner-mode: off            # STDIO 模式需禁用 Banner
  ai:
    mcp:
      server:
        name: filesystem-server
        version: 1.0.0

# MCP 文件读取配置
mcp:
  file:
    # 文件读取根目录限制（为空则不限制）
    read-root: D:/Project/Java/spring-ai-project
    # 允许读取的最大文件大小（字节），默认 1MB
    max-size: 1048576
```

### 3. 运行项目

```bash
# Maven 方式
mvn spring-boot:run

# JAR 方式
java -jar target/spring-ai-mcp-filesystem-1.0.0-SNAPSHOT.jar
```

启动成功后，日志显示：
```
Spring AI MCP 文件系统 Server 启动完成
Registered tools: 2
```

## MCP 配置

### 在 Cherry Studio 中使用

在 **设置 → MCP** 中添加以下配置：

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "java",
      "args": [
        "-jar",
        "D:/Project/Java/spring-ai-project/spring-ai-mcp/spring-ai-mcp-filesystem/target/spring-ai-mcp-filesystem-1.0.0-SNAPSHOT.jar"
      ]
    }
  }
}
```

**配置说明：**

| 字段 | 说明 | 示例 |
|------|------|------|
| `command` | 启动命令 | `java` |
| `args` | JAR 文件路径（数组格式） | `["-jar", "xxx.jar"]` |

**注意事项：**
- 使用绝对路径指向 JAR 文件
- `args` 必须是数组格式，每个参数独立元素
- 不需要 `cwd` 字段（部分客户端不支持）

### 在其他 MCP 客户端中使用

**Claude Code:**
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/spring-ai-mcp-filesystem-1.0.0-SNAPSHOT.jar"
      ]
    }
  }
}
```

## 可用工具

| 工具名 | 描述 | 参数 | 示例 |
|--------|------|------|------|
| `read_file` | 读取指定文件内容 | `filePath`: 文件路径 | `读取 pom.xml` |
| `get_read_root` | 获取配置的读取根目录 | 无 | `读取根目录是什么` |

## 使用示例

### 测试连接

```
问：MCP 文件系统的读取根目录是什么？
```

触发 `get_read_root` 工具，返回配置的 `read-root` 值。

### 读取文件

```
问：读取 D:/Project/Java/spring-ai-project/pom.xml 的内容
```

或简写：

```
问：读取 pom.xml
```

触发 `read_file` 工具，返回文件内容。

### 代码分析

```
问：读取 ChatController.java 并解释这个类的作用
```

MCP 读取文件后，LLM 会基于文件内容进行分析。

## 安全配置

| 配置项 | 说明 | 建议值 |
|--------|------|--------|
| `mcp.file.read-root` | 限制可读取的根目录 | 项目根目录 |
| `mcp.file.max-size` | 最大文件大小（字节） | `1048576` (1MB) |

**安全特性：**
- 目录穿越防护：禁止 `..` 路径
- 根目录范围限制：只能访问 `read-root` 下的文件
- 文件大小限制：避免内存溢出
- 仅支持 UTF-8 文本文件

## 常见问题

### Q: MCP Server 没有响应？

**检查项：**
1. JAR 文件路径是否正确（使用绝对路径）
2. Java 是否已安装并加入环境变量
3. 查看客户端日志，确认 MCP Server 是否正常启动

### Q: 如何查看 MCP Server 日志？

使用 Maven 运行可看到详细日志：
```bash
mvn spring-boot:run
```

### Q: 读取文件时提示"文件超出根目录范围"？

确保请求的文件路径在 `read-root` 配置的目录下，或扩大 `read-root` 范围。
