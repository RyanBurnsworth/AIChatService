package com.ryanburnsworth.ryanGpt.service;

import com.ryanburnsworth.ryanGpt.data.dto.ChatMessage;
import com.ryanburnsworth.ryanGpt.service.redis.RedisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    @Mock
    private RedisTemplate<String, ChatMessage> redisTemplate;

    @Mock
    private ListOperations<String, ChatMessage> listOperations;

    @InjectMocks
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock the template to return our listOperations when opsForList() is called
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void testSaveChatMessage() {
        ChatMessage chatMessage = getChatMessageDTO("Hello", "Hi there!", "fakeBase64==");

        redisService.saveChatMessage(chatMessage);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(listOperations, times(1)).leftPush(eq("chat_messages"), captor.capture());

        ChatMessage savedMessage = captor.getValue();
        assertEquals(chatMessage.getUserInput(), savedMessage.getUserInput());
        assertEquals(chatMessage.getAiOutput(), savedMessage.getAiOutput());
        assertEquals(chatMessage.getBase64Image(), savedMessage.getBase64Image());
    }

    @Test
    void testGetLatestChatMessages() {
        ChatMessage message1 = getChatMessageDTO("Hi", "Hello", null);
        ChatMessage message2 = getChatMessageDTO("How are you?", "I'm fine", null);

        when(listOperations.range("chat_messages", 0, 2)).thenReturn(List.of(message1, message2));

        List<ChatMessage> result = redisService.getLatestChatMessages(3);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hi", result.get(0).getUserInput());
        assertEquals("I'm fine", result.get(1).getAiOutput());

        verify(listOperations, times(1)).range("chat_messages", 0, 2);
    }

    @Test
    void testErrorFetchingChatMessages_ReturnsEmptyList() {
        when(listOperations.range("chat_messages", 0, 2))
                .thenThrow(new RuntimeException("Failed to reach Redis"));

        List<ChatMessage> result = redisService.getLatestChatMessages(3);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(listOperations, times(1)).range("chat_messages", 0, 2);
    }

    @Test
    void testErrorSavingChatMessage_Returns() {
        ChatMessage message = getChatMessageDTO("Hi", "Hello", null);

        when(redisTemplate.opsForList()).thenReturn(listOperations);

        doThrow(new RuntimeException("Redis down"))
                .when(listOperations).leftPush("chat_messages", message);

        assertDoesNotThrow(() -> redisService.saveChatMessage(message));

        verify(listOperations, times(1)).leftPush("chat_messages", message);
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    private ChatMessage getChatMessageDTO(String userInput, String aiOutput, String base64Image) {
        return ChatMessage.builder()
                .userInput(userInput)
                .aiOutput(aiOutput)
                .base64Image(base64Image)
                .build();
    }
}
