package com.ai.chat.test.mock;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

public class MockChatModel implements ChatModel {

    private String mockResponse;

    public MockChatModel(String mockResponse) {
        this.mockResponse = mockResponse;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return new ChatResponse(new Generation(mockResponse));
    }
}