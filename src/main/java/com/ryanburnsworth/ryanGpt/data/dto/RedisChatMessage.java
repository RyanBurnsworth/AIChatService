package com.ryanburnsworth.ryanGpt.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor      // Jackson needs this for deserialization
@AllArgsConstructor     // Builder and full-args constructor
public class RedisChatMessage {
    private String userInput;
    private String aiOutput;
    private String base64Image;
}