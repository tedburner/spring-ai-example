package com.ai.chat.application.advisor;

import com.ai.chat.application.interceptor.RateLimitingInterceptorService;
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
 * RateLimitAdvisor 单元测试
 *
 * @author kiturone
 * @date 2026/04/28
 */
class RateLimitAdvisorTest {

    private RateLimitingInterceptorService rateLimiter;
    private RateLimitAdvisor advisor;

    @BeforeEach
    void setUp() {
        rateLimiter = mock(RateLimitingInterceptorService.class);
        advisor = new RateLimitAdvisor(rateLimiter);
    }

    private ChatClientRequest createRequest(String text) {
        return new ChatClientRequest(new Prompt(new UserMessage(text)), new HashMap<>());
    }

    @Test
    void testOrder() {
        assertEquals(1, advisor.getOrder());
    }

    @Test
    void testBeforeCallsRateLimiterWithSessionId() {
        ChatClientRequest request = createRequest("测试");
        request.context().put("sessionId", "session-123");
        AdvisorChain chain = mock(AdvisorChain.class);

        advisor.before(request, chain);

        verify(rateLimiter, times(1)).checkRateLimit("session-123");
    }

    @Test
    void testBeforeWithDefaultSessionId() {
        ChatClientRequest request = createRequest("测试");
        AdvisorChain chain = mock(AdvisorChain.class);

        advisor.before(request, chain);

        verify(rateLimiter, times(1)).checkRateLimit("anonymous");
    }

    @Test
    void testBeforeThrowsOnRateLimitExceeded() {
        ChatClientRequest request = createRequest("测试");
        AdvisorChain chain = mock(AdvisorChain.class);

        doThrow(new IllegalStateException("请求频率过高")).when(rateLimiter).checkRateLimit(anyString());

        assertThrows(IllegalStateException.class, () -> advisor.before(request, chain));
    }

    @Test
    void testBeforeReturnsRequest() {
        ChatClientRequest request = createRequest("测试");
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
}
