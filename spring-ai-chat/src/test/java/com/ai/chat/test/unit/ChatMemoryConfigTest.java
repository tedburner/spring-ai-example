package com.ai.chat.test.unit;

import com.ai.chat.application.config.ChatMemoryProperties;
import com.ai.chat.application.config.ChatMemoryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ChatMemoryConfig.class, ChatMemoryProperties.class})
@TestPropertySource(properties = {
    "spring.ai.chat.memory.enabled=true",
    "spring.ai.chat.memory.type=in_memory",
    "spring.ai.chat.memory.ttl=5m",
    "spring.ai.chat.memory.capacity=5"
})
class ChatMemoryConfigTest {

    @Test
    void testChatMemoryProperties() {
        ChatMemoryProperties properties = new ChatMemoryProperties();
        properties.setEnabled(true);
        properties.setType("in_memory");
        properties.setTtl(java.time.Duration.ofMinutes(5));
        properties.setCapacity(5);

        assertTrue(properties.isEnabled());
        assertEquals("in_memory", properties.getType());
        assertEquals(5, properties.getCapacity());
        assertEquals(java.time.Duration.ofMinutes(5), properties.getTtl());
    }
}