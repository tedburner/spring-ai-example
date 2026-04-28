package com.ai.chat.interfaces.controller;

import com.ai.chat.application.metrics.MetricsCollector;
import com.ai.chat.application.service.PromptTemplateManager;
import com.ai.chat.application.service.ToolService;
import com.ai.chat.interfaces.dto.AdvisedQueryRequest;
import com.ai.chat.interfaces.dto.FunctionCallRequest;
import com.ai.chat.interfaces.dto.TemplateQueryRequest;
import com.ai.common.http.WebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天测试模型
 *
 * @author kiturone
 * @date 2025/5/2 18:06
 */
@RestController
@RequestMapping(value = "/chat")
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final ChatClient advisedChatClient;
    private final MetricsCollector metricsCollector;
    private final PromptTemplateManager promptTemplateManager;

    public ChatController(ChatClient.Builder chatClientBuilder,
                         MetricsCollector metricsCollector,
                         PromptTemplateManager promptTemplateManager,
                         List<Advisor> defaultAdvisors,
                         ToolService toolService) {
        this.metricsCollector = metricsCollector;
        this.promptTemplateManager = promptTemplateManager;

        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个有用的AI助手。")
                .build();

        this.advisedChatClient = chatClientBuilder
                .defaultSystem("你是一个有用的AI助手。请基于以下对话历史回答问题。")
                .defaultAdvisors(defaultAdvisors)
                .build();
    }

    /**
     * 流式对话接口
     */
    @GetMapping(value = "stream-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQuery(@RequestParam("query") String query) {
        LOGGER.info("问句：{}", query);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "stream-query");

        return chatClient.prompt()
                .user(query)
                .stream()
                .content()
                .doOnComplete(() -> {
                    long duration = Duration.between(startTime, Instant.now()).toMillis();
                    metricsCollector.recordChatDuration("deepseek-r1:8b", "stream-query", duration);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 非流式对话接口
     */
    @GetMapping(value = "query")
    public Mono<String> chat(@RequestParam("query") String query) {
        LOGGER.info("问句：{}", query);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "query");

        return Mono.fromCallable(() ->
                chatClient.prompt()
                        .user(query)
                        .call()
                        .content()
        )
        .doOnSuccess(result -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "query", duration);
            int tokenCount = result.length() / 4;
            metricsCollector.recordTokenUsage(tokenCount, "deepseek-r1:8b");
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 带记忆的流式对话接口
     */
    @GetMapping(value = "/memory/stream-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQueryWithMemory(
            @RequestParam("query") String query,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        LOGGER.info("带记忆问句：{}，会话ID：{}", query, sessionId);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "memory-stream-query");

        String actualSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        return advisedChatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, actualSessionId))
                .user(query)
                .stream()
                .content()
                .doOnComplete(() -> {
                    long duration = Duration.between(startTime, Instant.now()).toMillis();
                    metricsCollector.recordChatDuration("deepseek-r1:8b", "memory-stream-query", duration);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 带记忆的非流式对话接口
     */
    @GetMapping(value = "/memory/query")
    public Mono<String> chatWithMemory(
            @RequestParam("query") String query,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        LOGGER.info("带记忆问句：{}，会话ID：{}", query, sessionId);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "memory-query");

        String actualSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        return Mono.fromCallable(() ->
                advisedChatClient.prompt()
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, actualSessionId))
                        .user(query)
                        .call()
                        .content()
        )
        .doOnSuccess(result -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "memory-query", duration);
            int tokenCount = result.length() / 4;
            metricsCollector.recordTokenUsage(tokenCount, "deepseek-r1:8b");
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 基于模板的聊天接口
     */
    @PostMapping("/template-query")
    public Mono<WebResult> chatWithTemplate(@RequestBody TemplateQueryRequest request) {
        try {
            String prompt = promptTemplateManager.generatePrompt(
                request.getTemplateName(),
                request.getVariables()
            );

            Instant startTime = Instant.now();
            metricsCollector.incrementLlmCall("deepseek-r1:8b", "template-query");

            return Mono.fromCallable(() ->
                    chatClient.prompt()
                            .user(prompt)
                            .call()
                            .content()
            )
            .doOnSuccess(result -> {
                long duration = Duration.between(startTime, Instant.now()).toMillis();
                metricsCollector.recordChatDuration("deepseek-r1:8b", "template-query", duration);
            })
            .map(WebResult::buildSuccess)
            .onErrorReturn(WebResult.buildFail("模板查询失败"));
        } catch (Exception e) {
            LOGGER.error("模板查询失败", e);
            return Mono.just(WebResult.buildFail("模板查询失败: " + e.getMessage()));
        }
    }

    /**
     * 函数调用聊天接口
     */
    @PostMapping("/function-call")
    public Mono<WebResult> chatWithFunctionCalling(@RequestBody FunctionCallRequest request,
                                                   ToolCallbackProvider toolCallbackProvider) {
        try {
            Instant startTime = Instant.now();
            metricsCollector.incrementLlmCall("deepseek-r1:8b", "function-call");

            return Mono.fromCallable(() ->
                    chatClient.prompt()
                            .user(request.getQuery())
                            .tools(toolCallbackProvider)
                            .call()
                            .content()
            )
            .doOnSuccess(result -> {
                long duration = Duration.between(startTime, Instant.now()).toMillis();
                metricsCollector.recordChatDuration("deepseek-r1:8b", "function-call", duration);
            })
            .map(WebResult::buildSuccess)
            .onErrorReturn(WebResult.buildFail("函数调用失败"));
        } catch (Exception e) {
            LOGGER.error("函数调用失败", e);
            return Mono.just(WebResult.buildFail("函数调用失败: " + e.getMessage()));
        }
    }

    /**
     * 带 Advisor 的聊天接口
     * Advisors 通过 advisedChatClient 自动生效（限流、验证、日志、记忆）
     */
    @PostMapping("/advised-query")
    public Mono<WebResult> chatWithAdvisors(@RequestBody AdvisedQueryRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "advised-query");

        return Mono.fromCallable(() ->
                advisedChatClient.prompt()
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                        .user(request.getQuery())
                        .call()
                        .content()
        )
        .doOnSuccess(result -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "advised-query", duration);
        })
        .map(WebResult::buildSuccess)
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 获取所有可用的模板名称
     */
    @GetMapping("/templates")
    public WebResult getTemplateNames() {
        return WebResult.buildSuccess(promptTemplateManager.getTemplateNames());
    }

    /**
     * 添加自定义模板
     */
    @PostMapping("/templates")
    public WebResult addTemplate(@RequestParam String name, @RequestParam String template) {
        try {
            promptTemplateManager.addTemplate(name, template);
            return WebResult.buildSuccess("模板添加成功");
        } catch (Exception e) {
            return WebResult.buildFail("添加模板失败: " + e.getMessage());
        }
    }
}
