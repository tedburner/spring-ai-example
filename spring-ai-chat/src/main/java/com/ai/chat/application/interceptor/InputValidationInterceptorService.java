package com.ai.chat.application.interceptor;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 输入验证拦截器服务
 *
 * @author kiturone
 * @date 2026/4/8
 */
@Service
public class InputValidationInterceptorService {

    private static final int MAX_INPUT_LENGTH = 1000;

    // 安全风险关键词
    private static final List<String> SECURITY_KEYWORDS = Arrays.asList(
        "password", "token", "secret", "key", "credential"
    );

    // SQL 注入关键词
    private static final List<String> SQL_INJECTION_KEYWORDS = Arrays.asList(
        "select", "insert", "update", "delete", "drop", "create", "alter"
    );

    /**
     * 验证用户输入
     *
     * @param userText 用户输入文本
     * @throws IllegalArgumentException 如果输入不合法
     */
    public void validateInput(String userText) {
        // 检查空输入
        if (userText == null || userText.trim().isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }

        // 检查输入长度
        if (userText.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                String.format("输入长度超过限制（最大 %d 字符）", MAX_INPUT_LENGTH)
            );
        }

        // 检查安全风险关键词
        String lowerText = userText.toLowerCase();
        for (String keyword : SECURITY_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                throw new IllegalArgumentException(
                    String.format("输入包含敏感关键词: %s", keyword)
                );
            }
        }

        // 检查 SQL 注入
        for (String keyword : SQL_INJECTION_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                throw new IllegalArgumentException(
                    String.format("输入包含可能的 SQL 注入关键词: %s", keyword)
                );
            }
        }

        // 检查 XSS 攻击
        if (userText.contains("<script>") || userText.contains("</script>")) {
            throw new IllegalArgumentException("输入包含可能的 XSS 攻击代码");
        }
    }
}