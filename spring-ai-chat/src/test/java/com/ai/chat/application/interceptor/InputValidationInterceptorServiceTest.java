package com.ai.chat.application.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: kiturone
 * @date: 2026/4/8
 * @description: InputValidationInterceptorService 单元测试
 */
class InputValidationInterceptorServiceTest {

    private InputValidationInterceptorService validationInterceptor;

    @BeforeEach
    void setUp() {
        validationInterceptor = new InputValidationInterceptorService();
    }

    @Test
    void testValidInput() {
        String validText = "这是一个正常的问题";

        assertDoesNotThrow(() -> {
            validationInterceptor.validateInput(validText);
        });
    }

    @Test
    void testInputTooLong() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1100; i++) {
            longText.append("a");
        }

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(longText.toString());
        });
    }

    @Test
    void testInputWithPassword() {
        String textWithPassword = "我的password是123456";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(textWithPassword);
        });
    }

    @Test
    void testInputWithToken() {
        String textWithToken = "给我一个token";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(textWithToken);
        });
    }

    @Test
    void testInputWithSqlInjection() {
        String sqlInjection = "select * from users";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(sqlInjection);
        });
    }

    @Test
    void testInputWithDropTable() {
        String dropTable = "drop table users";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(dropTable);
        });
    }

    @Test
    void testInputWithScriptTag() {
        String xssAttack = "<script>alert('xss')</script>";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(xssAttack);
        });
    }

    @Test
    void testEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput("");
        });
    }

    @Test
    void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(null);
        });
    }

    @Test
    void testWhitespaceOnlyInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput("   ");
        });
    }

    @Test
    void testInputWithSecret() {
        String textWithSecret = "这是我的secret";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(textWithSecret);
        });
    }

    @Test
    void testInputWithKey() {
        String textWithKey = "我的key泄露了";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(textWithKey);
        });
    }

    @Test
    void testInputWithCredential() {
        String textWithCredential = "credential信息";

        assertThrows(IllegalArgumentException.class, () -> {
            validationInterceptor.validateInput(textWithCredential);
        });
    }
}