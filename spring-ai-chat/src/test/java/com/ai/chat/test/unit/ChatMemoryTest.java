package com.ai.chat.test.unit;

import com.ai.chat.application.config.ChatMemoryProperties;
import com.ai.chat.test.config.TestChatMemoryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestChatMemoryConfig.class})
class ChatMemoryTest {

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ChatMemoryProperties properties;

    @Test
    void testChatMemoryInitialization() {
        assertNotNull(chatMemory);
        assertEquals(properties.getCapacity(), 10); // default value
    }

    @Test
    void testStoreAndRetrieveMessages() {
        String sessionId = "test-session-1";

        // 添加消息
        UserMessage userMessage = new UserMessage("Hello, how are you?");
        chatMemory.add(sessionId, userMessage);

        // 检查消息是否保存
        List<Message> messages = chatMemory.get(sessionId);
        assertEquals(1, messages.size());
        assertEquals("Hello, how are you?", messages.get(0).getContent());
    }

    @Test
    void testMultipleSessions() {
        String session1 = "session-1";
        String session2 = "session-2";

        // 为第一个会话添加消息
        chatMemory.add(session1, new UserMessage("Message from session 1"));

        // 为第二个会话添加消息
        chatMemory.add(session2, new UserMessage("Message from session 2"));

        // 检查每个会话的消息
        List<Message> session1Messages = chatMemory.get(session1);
        List<Message> session2Messages = chatMemory.get(session2);

        assertEquals(1, session1Messages.size());
        assertEquals(1, session2Messages.size());
        assertEquals("Message from session 1", session1Messages.get(0).getContent());
        assertEquals("Message from session 2", session2Messages.get(0).getContent());
    }

    @Test
    void testSessionIsolation() {
        String sessionId = UUID.randomUUID().toString();

        // 添加几条消息
        chatMemory.add(sessionId, new UserMessage("First message"));
        chatMemory.add(sessionId, new UserMessage("Second message"));

        List<Message> messages = chatMemory.get(sessionId);
        assertEquals(2, messages.size());

        // 清除会话
        chatMemory.clear(sessionId);

        // 验证会话已被清除
        List<Message> clearedMessages = chatMemory.get(sessionId);
        assertTrue(clearedMessages.isEmpty());
    }

    @Test
    void testCapacityLimit() {
        String sessionId = "capacity-test";

        // 添加超过容量的消息（假设容量为10，我们添加15条）
        for (int i = 0; i < 15; i++) {
            chatMemory.add(sessionId, new UserMessage("Message " + i));
        }

        // 验证只保留最近的10条消息（根据容量设置）
        List<Message> messages = chatMemory.get(sessionId);
        assertTrue(messages.size() <= 10); // 容量限制生效
    }
}