package com.ryanburnsworth.ryanGpt.data.dto;

import lombok.Getter;
import lombok.Setter;

public class ChatRequest {
    @Getter
    @Setter
    String userInput;

    @Getter
    @Setter
    String base64Image;
}
