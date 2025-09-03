package com.ryanburnsworth.ryanGpt.service.databaseService;

import com.ryanburnsworth.ryanGpt.data.dto.ChatMessage;

import java.util.List;

public interface DatabaseService {
    void saveChatMessage(ChatMessage chatMessage);

    List<ChatMessage> getLatestChatMessages(int count);
}
