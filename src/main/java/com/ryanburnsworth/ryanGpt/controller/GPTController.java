package com.ryanburnsworth.ryanGpt.controller;

import com.ryanburnsworth.ryanGpt.data.GPTRequest;
import com.ryanburnsworth.ryanGpt.data.GPTResponse;
import org.springframework.http.ResponseEntity;

public interface GPTController {
    ResponseEntity<GPTResponse> generateResponse(GPTRequest gptRequest);
}
