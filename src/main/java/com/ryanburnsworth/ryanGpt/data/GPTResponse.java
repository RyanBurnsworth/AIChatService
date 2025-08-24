package com.ryanburnsworth.ryanGpt.data;

import lombok.Builder;
import lombok.Getter;

@Builder
public class GPTResponse {
    @Getter
    String response;
}
