package com.ryanburnsworth.ryanGpt.service.redis;

import com.ryanburnsworth.ryanGpt.data.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

import static com.ryanburnsworth.ryanGpt.utils.Constants.REDIS_CHAT_KEY;

@Slf4j
@Service
public class RedisServiceImpl implements RedisService { ;
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

        try {
            // store chat message into Redis
            template.opsForList().leftPush(REDIS_CHAT_KEY, chatMessage);

            // force list of chat messages to expire after 24 hours
            template.expire(REDIS_CHAT_KEY, Duration.ofHours(24));
        } catch (Exception e) {
            log.error("Error saving chat message to Redis: {}", e.getMessage());
        }
    }

    @Override
    public List<ChatMessage> getLatestChatMessages(int count) {
        try {
            return template.opsForList().range(REDIS_CHAT_KEY, 0, count - 1);
        } catch (Exception e) {
            log.error("Error get latest chat messages from Redis: {}", e.getMessage());
            return List.of();
        }
    }
}
