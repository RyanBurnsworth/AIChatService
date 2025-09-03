package com.ryanburnsworth.ryanGpt.service.databaseService;

import com.ryanburnsworth.ryanGpt.data.entities.ChatMessage;
import com.ryanburnsworth.ryanGpt.repository.ChatMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseServiceImpl implements DatabaseService {
    private final ChatMessageRepository repository;

    public DatabaseServiceImpl(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<com.ryanburnsworth.ryanGpt.data.dto.ChatMessage> getLatestChatMessages(int count) {
        List<com.ryanburnsworth.ryanGpt.data.dto.ChatMessage> messages = new ArrayList<>();
        List<ChatMessage> chatMessageEntities = this.repository.getLatestChatMessages(PageRequest.of(0, count));

        for (ChatMessage chatMessage : chatMessageEntities) {
            // Convert entity -> domain object (example)
            com.ryanburnsworth.ryanGpt.data.dto.ChatMessage msg = new com.ryanburnsworth.ryanGpt.data.dto.ChatMessage(
                    chatMessage.getUserInput(),
                    chatMessage.getAiOutput(),
                    chatMessage.getBase64Image()
            );
            messages.add(msg);
        }

        return messages;
    }

    @Override
    public void saveChatMessage(com.ryanburnsworth.ryanGpt.data.dto.ChatMessage chatMessage) {
        ChatMessage entity = new ChatMessage();
        entity.setUserInput(chatMessage.getUserInput());
        entity.setAiOutput(chatMessage.getAiOutput());
        entity.setBase64Image(chatMessage.getBase64Image());

        try {
            this.repository.save(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
