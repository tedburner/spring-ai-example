package com.ai.chat.interfaces.controller;

import com.ai.common.http.WebResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * Chat 模块全局异常处理
 *
 * @author kiturone
 * @date 2026/04/28
 * @description 统一处理 Advisor 链中抛出的异常
 */
@RestControllerAdvice(basePackages = "com.ai.chat.interfaces.controller")
public class ChatExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<WebResult> handleValidation(IllegalArgumentException e) {
        logger.warn("输入验证失败: {}", e.getMessage());
        return Mono.just(WebResult.buildFail("输入验证失败: " + e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Mono<WebResult> handleRateLimit(IllegalStateException e) {
        logger.warn("请求限流: {}", e.getMessage());
        return Mono.just(WebResult.buildFail("请求限流: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<WebResult> handleGeneral(Exception e) {
        logger.error("请求处理失败", e);
        return Mono.just(WebResult.buildFail("请求处理失败: " + e.getMessage()));
    }
}
