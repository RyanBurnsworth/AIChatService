package com.ryanburnsworth.ryanGpt.data;

import lombok.Getter;
import lombok.Setter;

public class GPTRequest {
    @Getter
    @Setter
    String userInput;

    @Getter
    @Setter
    String base64Image;
}
