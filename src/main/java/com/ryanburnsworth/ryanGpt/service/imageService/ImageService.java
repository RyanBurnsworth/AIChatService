package com.ryanburnsworth.ryanGpt.service.imageService;

public interface ImageService {
    String saveBase64Image(String base64Image);

    void deleteImageFile(String filename);
}
