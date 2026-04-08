package com.ai.chat.interfaces.controller;

import com.ai.chat.application.interceptor.InputValidationInterceptorService;
import com.ai.chat.application.interceptor.LoggingInterceptorService;
import com.ai.chat.application.interceptor.RateLimitingInterceptorService;
import com.ai.chat.application.metrics.MetricsCollector;
import com.ai.chat.application.service.PromptTemplateManager;
import com.ai.chat.interfaces.dto.AdvisedQueryRequest;
import com.ai.chat.interfaces.dto.FunctionCallRequest;
import com.ai.chat.interfaces.dto.TemplateQueryRequest;
import com.ai.common.http.WebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private final OllamaChatModel ollamaChatModel;
    private final MetricsCollector metricsCollector;
    private final PromptTemplateManager promptTemplateManager;
    private final LoggingInterceptorService loggingInterceptor;
    private final InputValidationInterceptorService validationInterceptor;
    private final RateLimitingInterceptorService rateLimitingInterceptor;

    // 用于存储对话历史的内存缓存
    private final Map<String, List<String>> chatHistory = new ConcurrentHashMap<>();

    public ChatController(OllamaChatModel ollamaChatModel,
                         MetricsCollector metricsCollector,
                         PromptTemplateManager promptTemplateManager,
                         LoggingInterceptorService loggingInterceptor,
                         InputValidationInterceptorService validationInterceptor,
                         RateLimitingInterceptorService rateLimitingInterceptor) {
        this.ollamaChatModel = ollamaChatModel;
        this.metricsCollector = metricsCollector;
        this.promptTemplateManager = promptTemplateManager;
        this.loggingInterceptor = loggingInterceptor;
        this.validationInterceptor = validationInterceptor;
        this.rateLimitingInterceptor = rateLimitingInterceptor;
    }

    /**
     * 流式对话接口
     *
     * @param query 问句
     * @return 结果
     */
    @GetMapping(value = "stream-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQuery(@RequestParam("query") String query) {
        LOGGER.info("问句：{}", query);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "stream-query");

        OllamaChatOptions options = OllamaChatOptions.builder()
                // 指定使用哪个大模型，这里使用的是deepseek-r1:8b模型
                .model("deepseek-r1:8b")
                .temperature(0.5D)
                .build();

        final Flux<ChatResponse> response = ollamaChatModel.stream(new Prompt(query, options));

        return response.doOnComplete(() -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "stream-query", duration);
        })
        .map(chatObj -> chatObj.getResult().getOutput().getText());
    }

    /**
     * 非流式对话接口
     *
     * @param query 问句
     * @return 结果
     */
    @GetMapping(value = "query")
    public Mono<String> chat(@RequestParam("query") String query) {
        LOGGER.info("问句：{}", query);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "query");

        OllamaChatOptions options = OllamaChatOptions.builder()
                // 指定使用哪个大模型，这里使用的是deepseek-r1:8b模型
                .model("deepseek-r1:8b")
                .temperature(0.5D)
                .build();
        return Mono.fromCallable(() -> {
            final ChatResponse response = ollamaChatModel.call(new Prompt(query, options));
            return response != null && response.getResult() != null && response.getResult().getOutput() != null
                    ? response.getResult().getOutput().getText()
                    : "";
        })
        .doOnSuccess(result -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "query", duration);

            // 记录大致 Token 使用量（估算）
            int tokenCount = result.length() / 4; // 粗略估算
            metricsCollector.recordTokenUsage(tokenCount, "deepseek-r1:8b");
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 带记忆的流式对话接口
     *
     * @param query     问句
     * @param sessionId 会话ID
     * @return 结果
     */
    @GetMapping(value = "/memory/stream-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQueryWithMemory(
            @RequestParam("query") String query,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        LOGGER.info("带记忆问句：{}，会话ID：{}", query, sessionId);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "memory-stream-query");

        String actualSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        // 获取历史对话并构造上下文
        String context = buildContext(actualSessionId, query);

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("deepseek-r1:8b")
                .temperature(0.5D)
                .build();

        // 创建包含上下文的提示
        Prompt prompt = new Prompt(context, options);

        final Flux<ChatResponse> response = ollamaChatModel.stream(prompt);

        return response.doOnComplete(() -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "memory-stream-query", duration);

            // 更新历史记录
            updateHistory(actualSessionId, query, null); // 异步更新
        })
        .map(chatObj -> chatObj.getResult().getOutput().getText());
    }

    /**
     * 带记忆的非流式对话接口
     *
     * @param query     问句
     * @param sessionId 会话ID
     * @return 结果
     */
    @GetMapping(value = "/memory/query")
    public Mono<String> chatWithMemory(
            @RequestParam("query") String query,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        LOGGER.info("带记忆问句：{}，会话ID：{}", query, sessionId);

        Instant startTime = Instant.now();
        metricsCollector.incrementLlmCall("deepseek-r1:8b", "memory-query");

        String actualSessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();

        // 获取历史对话并构造上下文
        String context = buildContext(actualSessionId, query);

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("deepseek-r1:8b")
                .temperature(0.5D)
                .build();

        // 创建包含上下文的提示
        Prompt prompt = new Prompt(context, options);

        return Mono.fromCallable(() -> {
            ChatResponse response = ollamaChatModel.call(prompt);
            String result = response != null && response.getResult() != null && response.getResult().getOutput() != null
                    ? response.getResult().getOutput().getText()
                    : "";

            // 更新历史记录
            updateHistory(actualSessionId, query, result);
            return result;
        })
        .doOnSuccess(result -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "memory-query", duration);

            // 记录大致 Token 使用量（估算）
            int tokenCount = result.length() / 4; // 粗略估算
            metricsCollector.recordTokenUsage(tokenCount, "deepseek-r1:8b");
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 构建包含上下文的提示
     */
    private String buildContext(String sessionId, String currentQuery) {
        StringBuilder context = new StringBuilder();

        // 添加系统提示
        context.append("你是一个有用的AI助手。请基于以下对话历史回答问题。\n\n");

        // 添加历史对话
        List<String> history = chatHistory.getOrDefault(sessionId, new ArrayList<>());
        for (String item : history) {
            context.append(item).append("\n");
        }

        // 添加当前问题
        context.append("用户: ").append(currentQuery).append("\n助手:");

        return context.toString();
    }

    /**
     * 更新对话历史
     */
    private void updateHistory(String sessionId, String query, String response) {
        chatHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
        List<String> history = chatHistory.get(sessionId);

        // 添加新的对话
        history.add("用户: " + query);
        if (response != null) {
            history.add("助手: " + response);
        }

        // 限制历史记录长度，防止内存溢出
        if (history.size() > 20) { // 保留最近20条记录
            history.subList(0, 5).clear(); // 移除最旧的5条记录
        }
    }

    // ========== Phase 2A 新功能 ==========

    /**
     * 基于模板的聊天接口
     *
     * @param request 模板查询请求
     * @return 结果
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

            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model("deepseek-r1:8b")
                    .temperature(0.5D)
                    .build();

            Prompt chatPrompt = new Prompt(prompt, options);
            ChatResponse response = ollamaChatModel.call(chatPrompt);

            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "template-query", duration);

            String result = response.getResult().getOutput().getText();
            return Mono.just(WebResult.buildSuccess(result));
        } catch (Exception e) {
            LOGGER.error("模板查询失败", e);
            return Mono.just(WebResult.buildFail("模板查询失败: " + e.getMessage()));
        }
    }

    /**
     * 函数调用聊天接口
     *
     * @param request 函数调用请求
     * @return 结果
     */
    @PostMapping("/function-call")
    public Mono<WebResult> chatWithFunctionCalling(@RequestBody FunctionCallRequest request) {
        try {
            Instant startTime = Instant.now();
            metricsCollector.incrementLlmCall("deepseek-r1:8b", "function-call");

            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model("deepseek-r1:8b")
                    .temperature(0.5D)
                    .build();

            Prompt prompt = new Prompt(request.getQuery(), options);
            ChatResponse response = ollamaChatModel.call(prompt);

            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "function-call", duration);

            String result = response.getResult().getOutput().getText();
            return Mono.just(WebResult.buildSuccess(result));
        } catch (Exception e) {
            LOGGER.error("函数调用失败", e);
            return Mono.just(WebResult.buildFail("函数调用失败: " + e.getMessage()));
        }
    }

    /**
     * 带 Advisor 的聊天接口
     *
     * @param request Advisor 增强查询请求
     * @return 结果
     */
    @PostMapping("/advised-query")
    public Mono<WebResult> chatWithAdvisors(@RequestBody AdvisedQueryRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        try {
            // 1. 限流检查
            rateLimitingInterceptor.checkRateLimit(sessionId);

            // 2. 输入验证
            validationInterceptor.validateInput(request.getQuery());

            // 3. 记录请求开始
            Instant startTime = loggingInterceptor.logRequestStart(sessionId, request.getQuery());

            metricsCollector.incrementLlmCall("deepseek-r1:8b", "advised-query");

            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model("deepseek-r1:8b")
                    .temperature(0.5D)
                    .build();

            Prompt prompt = new Prompt(request.getQuery(), options);

            ChatResponse response = ollamaChatModel.call(prompt);

            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "advised-query", duration);

            // 4. 记录请求结束
            loggingInterceptor.logRequestEnd(sessionId, startTime);

            String result = response.getResult().getOutput().getText();
            return Mono.just(WebResult.buildSuccess(result));
        } catch (IllegalArgumentException e) {
            LOGGER.error("输入验证失败", e);
            return Mono.just(WebResult.buildFail("输入验证失败: " + e.getMessage()));
        } catch (IllegalStateException e) {
            LOGGER.error("请求限流", e);
            return Mono.just(WebResult.buildFail("请求限流: " + e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Advisor 查询失败", e);
            loggingInterceptor.logRequestError(sessionId, e);
            return Mono.just(WebResult.buildFail("Advisor 查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有可用的模板名称
     *
     * @return 模板名称列表
     */
    @GetMapping("/templates")
    public WebResult getTemplateNames() {
        return WebResult.buildSuccess(promptTemplateManager.getTemplateNames());
    }

    /**
     * 添加自定义模板
     *
     * @param name 模板名称
     * @param template 模板内容
     * @return 结果
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
