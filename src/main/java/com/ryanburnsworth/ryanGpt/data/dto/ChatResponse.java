package com.ryanburnsworth.ryanGpt.data.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ChatResponse {
    @Getter
    String response;
}
