package com.ryanburnsworth.ryanGpt.controller;

import com.ryanburnsworth.ryanGpt.data.dto.ChatRequest;
import com.ryanburnsworth.ryanGpt.data.dto.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ryanburnsworth.ryanGpt.service.chatService.ChatService;

import static com.ryanburnsworth.ryanGpt.utils.Constants.CHAT_ENDPOINT;

@RestController
@RequestMapping(CHAT_ENDPOINT)
public class ChatControllerImpl implements ChatController {

    private final ChatService chatService;

    ChatControllerImpl(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    @PostMapping("")
    public ResponseEntity<ChatResponse> generateResponse(@RequestBody ChatRequest chatRequest) {
        if (chatRequest.getUserInput() == null || chatRequest.getUserInput().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String response = this.chatService.getResponse(chatRequest.getUserInput(), chatRequest.getBase64Image());

            ChatResponse chatResponse = ChatResponse.builder()
                    .response(response)
                    .build();

            return ResponseEntity.ok(chatResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
