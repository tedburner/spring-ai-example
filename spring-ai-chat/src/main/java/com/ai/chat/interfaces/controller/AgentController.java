package com.ai.chat.interfaces.controller;

import com.ai.chat.application.service.AgentService;
import com.ai.chat.application.service.WorkflowService;
import com.ai.chat.application.service.dto.ChainResult;
import com.ai.chat.application.service.dto.ParallelResult;
import com.ai.chat.application.service.dto.RoutingDecision;
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

import java.util.List;
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
    private final WorkflowService workflowService;

    public AgentController(AgentService agentService, WorkflowService workflowService) {
        this.agentService = agentService;
        this.workflowService = workflowService;
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

    /**
     * Chain Workflow — 顺序 LLM 调用管道
     */
    @PostMapping("/workflow/chain")
    public Mono<WebResult> chainWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String initialPrompt = (String) request.get("initialPrompt");
            @SuppressWarnings("unchecked")
            List<String> stepPrompts = (List<String>) request.get("stepPrompts");
            ChainResult result = workflowService.chainWorkflow(initialPrompt, stepPrompts);
            return Mono.just(WebResult.buildSuccess(result));
        } catch (Exception e) {
            LOGGER.error("Chain Workflow 执行失败", e);
            return Mono.just(WebResult.buildFail("Chain Workflow 执行失败：" + e.getMessage()));
        }
    }

    /**
     * Parallel Sectioning Workflow — 并发执行独立子任务
     */
    @PostMapping("/workflow/parallel/sectioning")
    public Mono<WebResult> parallelSectioning(@RequestBody Map<String, Object> request) {
        try {
            String mainPrompt = (String) request.get("mainPrompt");
            @SuppressWarnings("unchecked")
            List<String> sectionPrompts = (List<String>) request.get("sectionPrompts");
            ParallelResult result = workflowService.parallelSectioning(mainPrompt, sectionPrompts);
            return Mono.just(WebResult.buildSuccess(result));
        } catch (Exception e) {
            LOGGER.error("Parallel Sectioning 执行失败", e);
            return Mono.just(WebResult.buildFail("Parallel Sectioning 执行失败：" + e.getMessage()));
        }
    }

    /**
     * Parallel Voting Workflow — 多次调用取共识
     */
    @PostMapping("/workflow/parallel/voting")
    public Mono<WebResult> parallelVoting(@RequestBody Map<String, Object> request) {
        try {
            String prompt = (String) request.get("prompt");
            int votes = (int) request.getOrDefault("votes", 5);
            ParallelResult result = workflowService.parallelVoting(prompt, votes);
            return Mono.just(WebResult.buildSuccess(result));
        } catch (Exception e) {
            LOGGER.error("Parallel Voting 执行失败", e);
            return Mono.just(WebResult.buildFail("Parallel Voting 执行失败：" + e.getMessage()));
        }
    }

    /**
     * Routing Workflow — 输入分类路由
     */
    @PostMapping("/workflow/routing")
    public Mono<WebResult> routingWorkflow(@RequestBody Map<String, Object> request) {
        try {
            String input = (String) request.get("input");
            @SuppressWarnings("unchecked")
            Map<String, String> routePrompts = (Map<String, String>) request.get("routePrompts");
            RoutingDecision result = workflowService.routingWorkflow(input, routePrompts);
            return Mono.just(WebResult.buildSuccess(result));
        } catch (Exception e) {
            LOGGER.error("Routing Workflow 执行失败", e);
            return Mono.just(WebResult.buildFail("Routing Workflow 执行失败：" + e.getMessage()));
        }
    }
}
