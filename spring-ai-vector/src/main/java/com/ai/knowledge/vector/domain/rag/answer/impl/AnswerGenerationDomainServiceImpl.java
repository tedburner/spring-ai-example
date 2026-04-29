package com.ai.knowledge.vector.domain.rag.answer.impl;

import com.ai.knowledge.vector.domain.rag.answer.AnswerGenerationDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 Ollama ChatModel 的 RAG 答案生成实现。
 */
@Service
public class AnswerGenerationDomainServiceImpl implements AnswerGenerationDomainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnswerGenerationDomainServiceImpl.class);

    private final ChatModel chatModel;

    public AnswerGenerationDomainServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String generateAnswer(String query, List<Document> retrievedDocuments) {
        String context = retrievedDocuments.stream()
                .map(doc -> "- " + doc.getText())
                .collect(Collectors.joining("\n"));

        String promptText = """
                基于以下参考文档，回答用户的问题。如果参考文档中没有相关信息，请说明。

                参考文档：
                %s

                用户问题：%s

                请给出详细的回答：
                """.formatted(context, query);

        ChatOptions options = ChatOptions.builder()
                .temperature(0.3)
                .build();

        ChatResponse response = chatModel.call(new Prompt(promptText, options));
        String answer = response.getResult().getOutput().getText();
        LOGGER.info("RAG 答案生成完成，答案长度: {}", answer != null ? answer.length() : 0);
        return answer;
    }
}
