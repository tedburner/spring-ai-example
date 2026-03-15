package com.ai.mcp.filesystem.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件读取工具类
 *
 * 提供安全的本地文件读取功能，支持以下安全限制：
 * - 限制只能读取指定根目录下的文件，防止目录穿越攻击
 * - 限制文件最大大小，避免内存溢出
 * - 仅支持读取文本文件
 *
 * @author AI Assistant
 * @version 1.0
 */
@Component
public class FileReadTool {

    private static final Logger log = LoggerFactory.getLogger(FileReadTool.class);

    /**
     * 文件读取根目录限制
     * 所有文件读取操作都必须在此目录内
     */
    @Value("${mcp.file.read-root:}")
    private String readRoot;

    /**
     * 允许读取的最大文件大小（字节），默认 1MB
     */
    @Value("${mcp.file.max-size:1048576}")
    private long maxFileSize;

    /**
     * 读取指定文件的内容
     *
     * 安全校验流程：
     * 1. 检查路径是否为空
     * 2. 规范化路径并检查是否包含目录穿越字符
     * 3. 检查文件是否存在且可读
     * 4. 检查文件大小是否超过限制
     * 5. 检查文件是否在允许的根目录内
     *
     * @param filePath 要读取的文件路径（绝对路径或相对于 readRoot 的路径）
     * @return 文件内容字符串
     * @throws IllegalArgumentException 当路径无效或文件超出限制时抛出
     * @throws IOException 当文件读取失败时抛出
     */
    @Tool(name = "read_file", description = "读取本地文件的内容，仅支持文本文件")
    public String readFile(
            @ToolParam(description = "要读取的文件路径，可以是绝对路径或相对于配置根目录的路径")
            String filePath) {

        log.info("接收到文件读取请求：{}", filePath);

        try {
            // 1. 参数校验 - 检查路径是否为空
            if (filePath == null || filePath.trim().isEmpty()) {
                throw new IllegalArgumentException("文件路径不能为空");
            }

            // 2. 规范化路径 - 去除前后空格并替换反斜杠
            String normalizedPath = filePath.trim().replace("\\", "/");

            // 3. 安全检查 - 检测目录穿越攻击
            if (normalizedPath.contains("..")) {
                log.warn("检测到可疑的目录穿越路径：{}", filePath);
                throw new IllegalArgumentException("文件路径不能包含 '..' 字符");
            }

            // 4. 构建完整路径 - 如果是相对路径则拼接根目录
            Path targetPath = Paths.get(normalizedPath);
            if (!targetPath.isAbsolute() && readRoot != null && !readRoot.isEmpty()) {
                targetPath = Paths.get(readRoot, normalizedPath);
            }

            // 5. 规范化完整路径 - 解析符号链接和相对路径
            Path canonicalPath = targetPath.normalize();

            // 6. 根目录校验 - 确保文件在允许的读取范围内
            if (readRoot != null && !readRoot.isEmpty()) {
                Path rootPath = Paths.get(readRoot).normalize();
                if (!canonicalPath.startsWith(rootPath)) {
                    log.warn("文件超出允许的根目录范围：{}, 根目录：{}", canonicalPath, rootPath);
                    throw new IllegalArgumentException("文件路径超出允许的根目录范围");
                }
            }

            // 7. 文件存在性检查
            if (!Files.exists(canonicalPath)) {
                log.warn("文件不存在：{}", canonicalPath);
                throw new IllegalArgumentException("文件不存在：" + canonicalPath);
            }

            // 8. 文件可读性检查
            if (!Files.isReadable(canonicalPath)) {
                log.warn("文件不可读：{}", canonicalPath);
                throw new IllegalArgumentException("文件不可读：" + canonicalPath);
            }

            // 9. 文件大小检查 - 避免读取过大的文件导致内存溢出
            long fileSize = Files.size(canonicalPath);
            if (fileSize > maxFileSize) {
                log.warn("文件大小超出限制：{} 字节，最大允许：{} 字节", fileSize, maxFileSize);
                throw new IllegalArgumentException(
                    String.format("文件大小超出限制（最大 %d 字节）", maxFileSize));
            }

            // 10. 读取文件内容 - 使用 UTF-8 编码
            log.info("开始读取文件：{}, 大小：{} 字节", canonicalPath, fileSize);
            String content = Files.readString(canonicalPath, StandardCharsets.UTF_8);

            log.info("文件读取成功：{}, 内容长度：{}", canonicalPath, content.length());
            return content;

        } catch (IOException e) {
            log.error("文件读取失败：{}", filePath, e);
            throw new RuntimeException("文件读取失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取当前配置的文件读取根目录
     *
     * @return 根目录路径
     */
    @Tool(name = "get_read_root", description = "获取当前配置允许读取的文件根目录")
    public String getReadRoot() {
        return readRoot != null ? readRoot : "无限制（未配置 read-root）";
    }
}
