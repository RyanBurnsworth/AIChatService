package com.ryanburnsworth.ryanGpt.service;

public interface GPTService {
    String getResponse(String userInput, String base64Image);
}
