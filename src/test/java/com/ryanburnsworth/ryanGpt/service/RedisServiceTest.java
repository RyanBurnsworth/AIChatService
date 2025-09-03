package com.ryanburnsworth.ryanGpt.service;

import com.ryanburnsworth.ryanGpt.data.dto.RedisChatMessage;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    @Mock
    private RedisTemplate<String, RedisChatMessage> redisTemplate;

    @Mock
    private ListOperations<String, RedisChatMessage> listOperations;

    @InjectMocks
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock the template to return our listOperations when opsForList() is called
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void testSaveChatMessageToRedis() {
        // Arrange
        String userInput = "Hello";
        String aiOutput = "Hi there!";
        String base64Image = "fakeBase64==";

        // Act
        redisService.saveChatMessageToRedis(userInput, aiOutput, base64Image);

        // Assert
        ArgumentCaptor<RedisChatMessage> captor = ArgumentCaptor.forClass(RedisChatMessage.class);
        verify(listOperations, times(1)).leftPush(eq("chat_messages"), captor.capture());

        RedisChatMessage savedMessage = captor.getValue();
        assertEquals(userInput, savedMessage.getUserInput());
        assertEquals(aiOutput, savedMessage.getAiOutput());
        assertEquals(base64Image, savedMessage.getBase64Image());
    }

    @Test
    void testGetChatMessagesFromRedis() {
        // Arrange
        RedisChatMessage message1 = RedisChatMessage.builder()
                .userInput("Hi")
                .aiOutput("Hello")
                .base64Image(null)
                .build();
        RedisChatMessage message2 = RedisChatMessage.builder()
                .userInput("How are you?")
                .aiOutput("I'm fine")
                .base64Image(null)
                .build();

        when(listOperations.range("chat_messages", 0, 2)).thenReturn(List.of(message1, message2));

        // Act
        List<RedisChatMessage> result = redisService.getChatMessagesFromRedis(3);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hi", result.get(0).getUserInput());
        assertEquals("I'm fine", result.get(1).getAiOutput());

        verify(listOperations, times(1)).range("chat_messages", 0, 2);
    }
}
