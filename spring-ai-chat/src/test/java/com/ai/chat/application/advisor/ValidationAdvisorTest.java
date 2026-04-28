package com.ai.chat.application.advisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ValidationAdvisor 单元测试
 *
 * @author kiturone
 * @date 2026/04/28
 */
class ValidationAdvisorTest {

    private ValidationAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ValidationAdvisor();
    }

    private ChatClientRequest createRequest(String text) {
        return new ChatClientRequest(new Prompt(new UserMessage(text)), new HashMap<>());
    }

    @Test
    void testOrder() {
        assertEquals(2, advisor.getOrder());
    }

    @Test
    void testValidInput() {
        ChatClientRequest request = createRequest("你好，今天天气怎么样？");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertDoesNotThrow(() -> advisor.before(request, chain));
    }

    @Test
    void testEmptyInputCatchesNullEquivalent() {
        ChatClientRequest request = createRequest("");
        AdvisorChain chain = mock(AdvisorChain.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> advisor.before(request, chain));
        assertEquals("输入不能为空", ex.getMessage());
    }

    @Test
    void testWhitespaceInput() {
        ChatClientRequest request = createRequest("   ");
        AdvisorChain chain = mock(AdvisorChain.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> advisor.before(request, chain));
        assertEquals("输入不能为空", ex.getMessage());
    }

    @Test
    void testInputTooLong() {
        String longInput = "a".repeat(1001);
        ChatClientRequest request = createRequest(longInput);
        AdvisorChain chain = mock(AdvisorChain.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> advisor.before(request, chain));
        assertTrue(ex.getMessage().contains("输入长度超过限制"));
    }

    @Test
    void testMaxLengthInput() {
        String maxInput = "a".repeat(1000);
        ChatClientRequest request = createRequest(maxInput);
        AdvisorChain chain = mock(AdvisorChain.class);

        assertDoesNotThrow(() -> advisor.before(request, chain));
    }

    @Test
    void testSecurityKeywordPassword() {
        ChatClientRequest request = createRequest("我的 password 是什么？");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertThrows(IllegalArgumentException.class, () -> advisor.before(request, chain));
    }

    @Test
    void testSecurityKeywordToken() {
        ChatClientRequest request = createRequest("请给我 token");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertThrows(IllegalArgumentException.class, () -> advisor.before(request, chain));
    }

    @Test
    void testSqlInjectionSelect() {
        ChatClientRequest request = createRequest("select * from users");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertThrows(IllegalArgumentException.class, () -> advisor.before(request, chain));
    }

    @Test
    void testSqlInjectionDrop() {
        ChatClientRequest request = createRequest("DROP TABLE users");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertThrows(IllegalArgumentException.class, () -> advisor.before(request, chain));
    }

    @Test
    void testXssAttack() {
        ChatClientRequest request = createRequest("<script>alert('xss')</script>");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertThrows(IllegalArgumentException.class, () -> advisor.before(request, chain));
    }

    @Test
    void testAfterReturnsResponse() {
        ChatClientResponse response = mock(ChatClientResponse.class);
        AdvisorChain chain = mock(AdvisorChain.class);

        assertSame(response, advisor.after(response, chain));
    }
}
