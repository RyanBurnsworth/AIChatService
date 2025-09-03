package com.ryanburnsworth.ryanGpt.service;

import com.ryanburnsworth.ryanGpt.data.entities.ChatMessage;
import com.ryanburnsworth.ryanGpt.repository.ChatMessageRepository;
import com.ryanburnsworth.ryanGpt.service.databaseService.DatabaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseServiceTest {

    private ChatMessageRepository mockRepository;
    private DatabaseServiceImpl databaseService;

    @BeforeEach
    void setup() {
        mockRepository = mock(ChatMessageRepository.class);
        databaseService = new DatabaseServiceImpl(mockRepository);
    }

    @Test
    void getLatestChatMessages_ShouldReturnMappedMessages() {
        ChatMessage entity1 = new ChatMessage();
        entity1.setUserInput("Hello");
        entity1.setAiOutput("Hi there!");
        entity1.setBase64Image(null);

        ChatMessage entity2 = new ChatMessage();
        entity2.setUserInput("How are you?");
        entity2.setAiOutput("I'm fine.");
        entity2.setBase64Image(null);

        when(mockRepository.getLatestChatMessages(PageRequest.of(0, 2)))
                .thenReturn(List.of(entity1, entity2));

        List<com.ryanburnsworth.ryanGpt.data.dto.ChatMessage> messages = databaseService.getLatestChatMessages(2);

        assertEquals(2, messages.size());
        assertEquals("Hello", messages.get(0).getUserInput());
        assertEquals("Hi there!", messages.get(0).getAiOutput());
        assertEquals("How are you?", messages.get(1).getUserInput());
        assertEquals("I'm fine.", messages.get(1).getAiOutput());

        verify(mockRepository).getLatestChatMessages(PageRequest.of(0, 2));
    }

    @Test
    void saveChatMessage_ShouldSaveEntityInRepository() {
        com.ryanburnsworth.ryanGpt.data.dto.ChatMessage dto = com.ryanburnsworth.ryanGpt.data.dto.ChatMessage.builder()
                .userInput("User input")
                .aiOutput("AI Output")
                .base64Image("base64image")
                .build();

        databaseService.saveChatMessage(dto);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(mockRepository).save(captor.capture());

        ChatMessage savedEntity = captor.getValue();
        assertEquals(dto.getUserInput(), savedEntity.getUserInput());
        assertEquals(dto.getAiOutput(), savedEntity.getAiOutput());
        assertEquals(dto.getBase64Image(), savedEntity.getBase64Image());
    }

    @Test
    void saveChatMessage_WhenRepositoryThrowsException_ShouldNotFail() {
        com.ryanburnsworth.ryanGpt.data.dto.ChatMessage dto = com.ryanburnsworth.ryanGpt.data.dto.ChatMessage.builder()
                .userInput("User input")
                .aiOutput("AI Output")
                .base64Image(null)
                .build();

        doThrow(new RuntimeException("DB error")).when(mockRepository).save(any(ChatMessage.class));

        assertDoesNotThrow(() -> databaseService.saveChatMessage(dto));

        verify(mockRepository).save(any(ChatMessage.class));
    }
}
