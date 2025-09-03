package com.ryanburnsworth.ryanGpt.controller;

import com.ryanburnsworth.ryanGpt.data.dto.ChatRequest;
import com.ryanburnsworth.ryanGpt.data.dto.ChatResponse;
import com.ryanburnsworth.ryanGpt.service.chatService.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatControllerImpl chatController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateResponse_success() throws Exception {
        // Arrange
        String userInput = "Hello";
        String base64Image = null;
        String expectedResponse = "Hi there!";

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setBase64Image(base64Image);
        chatRequest.setUserInput(userInput);

        when(chatService.getResponse(userInput, base64Image)).thenReturn(expectedResponse);

        ResponseEntity<ChatResponse> responseEntity = chatController.generateResponse(chatRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(expectedResponse, responseEntity.getBody().getResponse());

        verify(chatService, times(1)).getResponse(userInput, base64Image);
    }

    @Test
    void testGenerateResponse_badRequest_whenUserInputIsEmpty() {
        // Arrange
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setBase64Image(null);
        chatRequest.setUserInput("");

        ResponseEntity<ChatResponse> responseEntity = chatController.generateResponse(chatRequest);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        verify(chatService, never()).getResponse(anyString(), any());
    }

    @Test
    void testGenerateResponse_internalServerError_onException() throws Exception {
        // Arrange
        String userInput = "Hello";
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setBase64Image(null);
        chatRequest.setUserInput(userInput);
        when(chatService.getResponse(anyString(), any())).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<ChatResponse> responseEntity = chatController.generateResponse(chatRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());

        verify(chatService, times(1)).getResponse(userInput, null);
    }
}
