package com.ryanburnsworth.ryanGpt.service.redis;

import com.ryanburnsworth.ryanGpt.data.dto.RedisChatMessage;

import java.util.List;

public interface RedisService {
    void saveChatMessageToRedis(String userInput, String aiOutput, String base64Image);

    List<RedisChatMessage> getChatMessagesFromRedis(int count);
}
