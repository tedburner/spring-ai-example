package com.ai.chat.interfaces.controller;

import com.ai.chat.application.metrics.MetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
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

    private final OllamaChatModel ollamaChatModel;
    private final ChatMemory chatMemory;
    private final MetricsCollector metricsCollector;

    public ChatController(OllamaChatModel ollamaChatModel, ChatMemory chatMemory, MetricsCollector metricsCollector) {
        this.ollamaChatModel = ollamaChatModel;
        this.chatMemory = chatMemory;
        this.metricsCollector = metricsCollector;
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

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("deepseek-r1:8b")
                .temperature(0.5D)
                .build();

        Prompt prompt = new Prompt(query, options);

        final Flux<ChatResponse> response = ollamaChatModel.stream(prompt);

        return response.doOnComplete(() -> {
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            metricsCollector.recordChatDuration("deepseek-r1:8b", "memory-stream-query", duration);
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

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("deepseek-r1:8b")
                .temperature(0.5D)
                .build();

        Prompt prompt = new Prompt(query, options);

        return Mono.fromCallable(() -> {
            ChatResponse response = ollamaChatModel.call(prompt);
            return response != null && response.getResult() != null && response.getResult().getOutput() != null
                    ? response.getResult().getOutput().getText()
                    : "";
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
}
