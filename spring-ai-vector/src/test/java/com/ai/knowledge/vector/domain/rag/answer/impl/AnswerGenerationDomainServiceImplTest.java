package com.ai.knowledge.vector.domain.rag.answer.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AnswerGenerationDomainServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AnswerGenerationDomainServiceImplTest {

    @Mock
    private ChatModel chatModel;

    private AnswerGenerationDomainServiceImpl answerService;

    @BeforeEach
    void setUp() {
        answerService = new AnswerGenerationDomainServiceImpl(chatModel);
    }

    @Test
    void testGenerateAnswerWithDocuments() {
        List<Document> docs = List.of(
                new Document("北京是中国的首都"),
                new Document("北京有超过2100万常住人口")
        );
        when(chatModel.call(any(Prompt.class))).thenReturn(createChatResponse("北京是中国的首都，拥有超过2100万常住人口。"));

        String result = answerService.generateAnswer("北京有哪些特点？", docs);

        assertNotNull(result);
        assertEquals("北京是中国的首都，拥有超过2100万常住人口。", result);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void testGenerateAnswerWithEmptyDocuments() {
        List<Document> docs = List.of();
        when(chatModel.call(any(Prompt.class))).thenReturn(createChatResponse("没有相关信息"));

        String result = answerService.generateAnswer("未知问题", docs);

        assertNotNull(result);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void testGenerateAnswerWithSingleDocument() {
        List<Document> docs = List.of(new Document("单条文档内容"));
        when(chatModel.call(any(Prompt.class))).thenReturn(createChatResponse("基于文档的回答"));

        String result = answerService.generateAnswer("问题", docs);

        assertEquals("基于文档的回答", result);
    }

    private ChatResponse createChatResponse(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
