package com.ryanburnsworth.ryanGpt.service.redis;

import com.ryanburnsworth.ryanGpt.data.dto.ChatMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisServiceImpl implements RedisService {
    private static final String REDIS_CHAT_KEY = "chat_messages";

    private final RedisTemplate<String, ChatMessage> template;

    public RedisServiceImpl(RedisTemplate<String, ChatMessage> template) {
        this.template = template;
    }

    @Override
    public void saveChatMessage(ChatMessage chatMessage) {
        ChatMessage message = ChatMessage.builder()
                .userInput(chatMessage.getUserInput())
                .aiOutput(chatMessage.getAiOutput())
                .base64Image(chatMessage.getBase64Image())
                .build();

        // store chat message into Redis
        template.opsForList().leftPush(REDIS_CHAT_KEY, chatMessage);

        // force list of chat messages to expire after 24 hours
        template.expire(REDIS_CHAT_KEY, Duration.ofHours(24));
    }

    @Override
    public List<ChatMessage> getLatestChatMessages(int count) {
        return template.opsForList().range(REDIS_CHAT_KEY, 0, count - 1);
    }
}
