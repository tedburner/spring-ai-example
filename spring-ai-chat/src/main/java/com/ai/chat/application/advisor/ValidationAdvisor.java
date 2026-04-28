package com.ai.chat.application.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 输入验证 Advisor -- 替代 InputValidationInterceptorService
 *
 * @author kiturone
 * @date 2026/04/28
 * @description 在执行 LLM 调用前对用户输入进行安全校验
 */
public class ValidationAdvisor implements BaseAdvisor {

    private static final int MAX_INPUT_LENGTH = 1000;

    private static final List<String> SECURITY_KEYWORDS = Arrays.asList(
        "password", "token", "secret", "key", "credential"
    );

    private static final List<String> SQL_INJECTION_KEYWORDS = Arrays.asList(
        "select", "insert", "update", "delete", "drop", "create", "alter"
    );

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String userText = request.prompt().getUserMessage().getText();

        if (userText == null || userText.trim().isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }

        if (userText.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException(
                String.format("输入长度超过限制（最大 %d 字符）", MAX_INPUT_LENGTH)
            );
        }

        String lowerText = userText.toLowerCase();
        for (String keyword : SECURITY_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                throw new IllegalArgumentException(
                    String.format("输入包含敏感关键词: %s", keyword)
                );
            }
        }

        for (String keyword : SQL_INJECTION_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                throw new IllegalArgumentException(
                    String.format("输入包含可能的 SQL 注入关键词: %s", keyword)
                );
            }
        }

        if (userText.contains("<script>") || userText.contains("</script>")) {
            throw new IllegalArgumentException("输入包含可能的 XSS 攻击代码");
        }

        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }
}
