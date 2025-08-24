package com.ryanburnsworth.ryanGpt.controller;

import com.ryanburnsworth.ryanGpt.data.GPTRequest;
import com.ryanburnsworth.ryanGpt.data.GPTResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ryanburnsworth.ryanGpt.service.GPTService;

@RestController
@RequestMapping("/api/v1/gpt")
public class GPTControllerImpl implements GPTController {

    private final GPTService gptService;

    GPTControllerImpl(GPTService gptService) {
        this.gptService = gptService;
    }

    @Override
    @PostMapping("")
    public ResponseEntity<GPTResponse> generateResponse(@RequestBody GPTRequest gptRequest) {
        if (gptRequest.getUserInput() == null || gptRequest.getUserInput().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String response = this.gptService.getResponse(gptRequest.getUserInput(), gptRequest.getBase64Image());

            GPTResponse gptResponse = GPTResponse.builder()
                    .response(response)
                    .build();

            return ResponseEntity.ok(gptResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
