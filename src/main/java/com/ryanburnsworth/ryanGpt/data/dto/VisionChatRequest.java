package com.ryanburnsworth.ryanGpt.data.dto;

import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VisionChatRequest {
    String filename;

    ChatRequest chatRequest;
}
