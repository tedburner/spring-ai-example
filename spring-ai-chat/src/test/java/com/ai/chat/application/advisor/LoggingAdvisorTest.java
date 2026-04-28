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
 * LoggingAdvisor 单元测试
 *
 * @author kiturone
 * @date 2026/04/28
 */
class LoggingAdvisorTest {

    private LoggingAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new LoggingAdvisor();
    }

    private ChatClientRequest createRequest(String text) {
        return new ChatClientRequest(new Prompt(new UserMessage(text)), new HashMap<>());
    }

    @Test
    void testOrder() {
        assertEquals(3, advisor.getOrder());
    }

    @Test
    void testBeforeReturnsRequest() {
        ChatClientRequest request = createRequest("测试消息");
        AdvisorChain chain = mock(AdvisorChain.class);

        ChatClientRequest result = advisor.before(request, chain);

        assertSame(request, result);
    }

    @Test
    void testAfterReturnsResponse() {
        ChatClientResponse response = mock(ChatClientResponse.class);
        AdvisorChain chain = mock(AdvisorChain.class);

        ChatClientResponse result = advisor.after(response, chain);

        assertSame(response, result);
    }

    @Test
    void testBeforeWithSessionId() {
        ChatClientRequest request = createRequest("测试消息");
        request.context().put("sessionId", "test-session-123");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertDoesNotThrow(() -> advisor.before(request, chain));
    }

    @Test
    void testBeforeWithoutSessionId() {
        ChatClientRequest request = createRequest("测试消息");
        AdvisorChain chain = mock(AdvisorChain.class);

        assertDoesNotThrow(() -> advisor.before(request, chain));
    }

    @Test
    void testBeforeAndAfterSequence() {
        ChatClientRequest request = createRequest("Hello");
        AdvisorChain chain = mock(AdvisorChain.class);

        advisor.before(request, chain);
        ChatClientResponse response = mock(ChatClientResponse.class);

        assertDoesNotThrow(() -> advisor.after(response, chain));
    }
}
