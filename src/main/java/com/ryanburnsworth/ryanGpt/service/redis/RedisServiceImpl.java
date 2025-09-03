package com.ryanburnsworth.ryanGpt.service.redis;

import com.ryanburnsworth.ryanGpt.data.dto.RedisChatMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisServiceImpl implements RedisService {
    private static final String REDIS_CHAT_KEY = "chat_messages";

    private final RedisTemplate<String, RedisChatMessage> template;

    public RedisServiceImpl(RedisTemplate<String, RedisChatMessage> template) {
        this.template = template;
    }

    @Override
    public void saveChatMessageToRedis(String userInput, String aiOutput, String base64Image) {
        RedisChatMessage chatMessage = RedisChatMessage.builder()
                .userInput(userInput)
                .aiOutput(aiOutput)
                .base64Image(base64Image)
                .build();

        // store chat message into Redis
        template.opsForList().leftPush(REDIS_CHAT_KEY, chatMessage);
    }

    @Override
    public List<RedisChatMessage> getChatMessagesFromRedis(int count) {
        return template.opsForList().range(REDIS_CHAT_KEY, 0, count - 1);
    }
}
