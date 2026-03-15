package com.ai.mcp.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.ai.mcp.filesystem.tools.FileReadTool;

/**
 * Spring AI MCP 文件系统服务器启动类
 *
 * 此应用作为一个 MCP Server，通过 STDIO 传输协议与客户端通信。
 * 提供文件读取等工具功能供 LLM 调用。
 *
 * 配置说明：
 * - web-application-type: none (禁用 Web 服务)
 * - banner-mode: off (关闭 Banner，避免干扰 STDIO 通信)
 *
 * @author AI Assistant
 * @version 1.0
 */
@SpringBootApplication
public class SpringAiMcpFilesystemApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringAiMcpFilesystemApplication.class);

    public static void main(String[] args) {
        log.info("正在启动 Spring AI MCP 文件系统 Server...");
        SpringApplication.run(SpringAiMcpFilesystemApplication.class, args);
        log.info("Spring AI MCP 文件系统 Server 启动完成");
    }

    /**
     * 注册文件读取工具的 ToolCallbackProvider
     *
     * 此 Bean 将 FileReadTool 中的@Tool 注解方法注册为 MCP 可调用的工具。
     * Spring AI 会自动扫描@Tool 注解并暴露为 MCP Tool。
     *
     * @param fileReadTool 文件读取工具实例（由 Spring 容器注入）
     * @return ToolCallbackProvider 用于提供工具回调
     */
    @Bean
    public ToolCallbackProvider fileReadToolProvider(FileReadTool fileReadTool) {
        log.info("注册 FileReadTool 为 MCP 工具");
        // 使用 MethodToolCallbackProvider 从指定类中提取@Tool 注解方法
        return MethodToolCallbackProvider.builder()
                .toolObjects(fileReadTool)  // 注册包含@Tool 方法的对象
                .build();
    }
}
