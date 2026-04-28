package com.ai.chat.application.advisor;

import com.ai.chat.application.interceptor.RateLimitingInterceptorService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import java.util.Map;

/**
 * 限流 Advisor -- 包装 RateLimitingInterceptorService
 *
 * @author kiturone
 * @date 2026/04/28
 * @description 在 Advisor 链中执行限流检查，作为最先的拦截点
 */
public class RateLimitAdvisor implements BaseAdvisor {

    private final RateLimitingInterceptorService rateLimiter;

    public RateLimitAdvisor(RateLimitingInterceptorService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        Map<String, Object> context = request.context();
        String sessionId = (String) context.getOrDefault("sessionId", "anonymous");
        rateLimiter.checkRateLimit(sessionId);
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }
}
