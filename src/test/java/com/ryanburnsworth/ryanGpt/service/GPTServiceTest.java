package com.ryanburnsworth.ryanGpt.service;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GPTServiceImplTest {

    @Mock
    private SimpleOpenAI mockOpenAIClient;

    private GPTServiceImpl gptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create instance using reflection/mock injection
        gptService = new GPTServiceImpl("fakeKey", "fakeOrg", "fakeProject");
    }

    @Test
    void getResponse_TextOnly_ReturnsExpectedString() {
        // Arrange
        String userInput = "Hello GPT!";
        Chat mockChat = mock(Chat.class);
        when(mockChat.getChoices()).thenReturn(List.of("choice1"));
        when(mockChat.firstContent()).thenReturn("Hello Response");
        Stream<Chat> mockStream = Stream.of(mockChat);

        when(mockOpenAIClient.chatCompletions().createStream(any(ChatRequest.class)).join())
                .thenReturn(mockStream);

        String response = gptService.getResponse(userInput, null);

        assertEquals("Hello Response", response);

        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(mockOpenAIClient.chatCompletions(), times(1)).createStream(captor.capture());
        ChatRequest sentRequest = captor.getValue();
        assertEquals(userInput, sentRequest.getMessages().get(1).getContent().get(0).getText());
    }

    @Test
    void getResponse_WithImage_ReturnsExpectedString() {
        // Arrange
        String userInput = "Describe this image";
        String base64Image = "base64data";
        Chat mockChat = mock(Chat.class);
        when(mockChat.getChoices()).thenReturn(List.of("choice1"));
        when(mockChat.firstContent()).thenReturn("Image description");
        Stream<Chat> mockStream = Stream.of(mockChat);

        when(mockOpenAIClient.chatCompletions().createStream(any(ChatRequest.class)).join())
                .thenReturn(mockStream);

        String response = gptService.getResponse(userInput, base64Image);

        assertEquals("Image description", response);

        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(mockOpenAIClient.chatCompletions(), times(1)).createStream(captor.capture());
        ChatRequest sentRequest = captor.getValue();
        assertEquals(base64Image, sentRequest.getMessages().get(0).getContent().get(1).getImageUrl().getUrl());
    }
}
