package com.ai.chat.interfaces.controller;

import com.ai.chat.application.service.AgentService;
import com.ai.chat.domain.entity.TavilySearchResponse;
import com.ai.chat.interfaces.dto.AgentChatRequest;
import com.ai.chat.interfaces.dto.TavilySearchRequest;
import com.ai.common.http.WebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Agent REST 控制器
 *
 * @author kiturone
 * @date 2026/04/10
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentController.class);

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 非流式 Agent 对话
     *
     * @param request 请求
     * @return 响应
     */
    @PostMapping("/chat")
    public Mono<WebResult> chat(@RequestBody AgentChatRequest request) {
        try {
            String response = agentService.chat(request);
            return Mono.just(WebResult.buildSuccess(Map.of(
                    "message", response,
                    "sessionId", request.getSessionId()
            )));
        } catch (Exception e) {
            LOGGER.error("Agent 对话失败", e);
            return Mono.just(WebResult.buildFail("Agent 对话失败：" + e.getMessage()));
        }
    }

    /**
     * 流式 Agent 对话（SSE）
     *
     * @param request 请求
     * @return 响应流
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody AgentChatRequest request) {
        return agentService.streamChat(request);
    }

    /**
     * 带会话记忆的 Agent 对话
     *
     * @param request 请求
     * @return 响应
     */
    @PostMapping("/chat/memory")
    public Mono<WebResult> chatWithMemory(@RequestBody AgentChatRequest request) {
        try {
            String response = agentService.chatWithMemory(request);
            return Mono.just(WebResult.buildSuccess(Map.of(
                    "message", response,
                    "sessionId", request.getSessionId()
            )));
        } catch (Exception e) {
            LOGGER.error("Agent 带记忆对话失败", e);
            return Mono.just(WebResult.buildFail("Agent 带记忆对话失败：" + e.getMessage()));
        }
    }

    /**
     * Tavily 网络搜索
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    @PostMapping("/tavily/search")
    public Mono<WebResult> tavilySearch(@RequestBody TavilySearchRequest request) {
        try {
            TavilySearchResponse response = agentService.tavilySearch(request);
            return Mono.just(WebResult.buildSuccess(response));
        } catch (Exception e) {
            LOGGER.error("Tavily 搜索失败", e);
            return Mono.just(WebResult.buildFail("Tavily 搜索失败：" + e.getMessage()));
        }
    }

    /**
     * 简化的搜索接口（GET）
     *
     * @param query 查询词
     * @return 搜索结果
     */
    @GetMapping("/tavily/search")
    public Mono<WebResult> tavilySearchGet(@RequestParam String query) {
        try {
            TavilySearchRequest request = TavilySearchRequest.builder()
                    .query(query)
                    .maxResults(5)
                    .searchDepth("basic")
                    .includeAnswer(true)
                    .build();

            TavilySearchResponse response = agentService.tavilySearch(request);
            return Mono.just(WebResult.buildSuccess(response));
        } catch (Exception e) {
            LOGGER.error("Tavily 搜索失败", e);
            return Mono.just(WebResult.buildFail("Tavily 搜索失败：" + e.getMessage()));
        }
    }
}
