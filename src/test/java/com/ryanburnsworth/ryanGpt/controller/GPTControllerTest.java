package com.ryanburnsworth.ryanGpt.controller;

import com.ryanburnsworth.ryanGpt.data.GPTRequest;
import com.ryanburnsworth.ryanGpt.data.GPTResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import com.ryanburnsworth.ryanGpt.service.GPTService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GPTControllerTest {

    @Mock
    private GPTService gptService;

    @InjectMocks
    private GPTControllerImpl gptController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateResponse_ShouldReturnGPTResponse() {
        GPTRequest request = new GPTRequest();
        request.setUserInput("What’s in this image?");
        request.setBase64Image("base64ImageData");

        String mockResponse = "This is a cat.";
        when(gptService.getResponse(request.getUserInput(), request.getBase64Image()))
                .thenReturn(mockResponse);

        ResponseEntity<GPTResponse> responseEntity = gptController.generateResponse(request);

        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        assertNotNull(responseEntity.getBody());
        assertEquals(mockResponse, responseEntity.getBody().getResponse());
    }

    @Test
    void generateResponse_ShouldReturn400_WhenUserInputIsEmpty() {
        GPTRequest request = new GPTRequest();
        request.setUserInput("");
        request.setBase64Image(null);

        when(gptService.getResponse(request.getUserInput(), request.getBase64Image()))
                .thenThrow(new RuntimeException("Service failure"));

        ResponseEntity<GPTResponse> responseEntity = gptController.generateResponse(request);

        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    void generateResponse_ShouldReturn500_WhenServiceThrowsException() {
        GPTRequest request = new GPTRequest();
        request.setUserInput("cause failure");
        request.setBase64Image("badImage");

        when(gptService.getResponse(request.getUserInput(), request.getBase64Image()))
                .thenThrow(new RuntimeException("Service failure"));

        ResponseEntity<GPTResponse> responseEntity = gptController.generateResponse(request);

        assertTrue(responseEntity.getStatusCode().is5xxServerError());
        assertNull(responseEntity.getBody());
    }
}
