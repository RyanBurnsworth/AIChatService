package com.ryanburnsworth.ryanGpt.controller;

import com.ryanburnsworth.ryanGpt.data.dto.ChatRequest;
import com.ryanburnsworth.ryanGpt.data.dto.ChatResponse;
import org.springframework.http.ResponseEntity;

public interface ChatController {
    ResponseEntity<ChatResponse> generateResponse(ChatRequest chatRequest);
}
