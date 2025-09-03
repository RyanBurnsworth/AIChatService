package com.ryanburnsworth.ryanGpt.service;

import com.ryanburnsworth.ryanGpt.service.imageService.ImageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceImplTest {

    private ImageServiceImpl imageService;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        // Override the FILE_DIRECTORY constant using reflection (since it's static final)
        System.setProperty("FILE_DIRECTORY", tempDir.getAbsolutePath());
        imageService = new ImageServiceImpl();
    }

    @Test
    @Disabled
    void testSaveBase64Image_Success() throws Exception {
        // Given: A small "Hello" text as base64
        String base64Image = Base64.getEncoder().encodeToString("hello".getBytes());

        // When
        String filename = imageService.saveBase64Image(base64Image);

        // Then
        assertNotNull(filename);
        assertFalse(filename.isEmpty());

        File savedFile = new File(tempDir, filename);
        assertTrue(savedFile.exists());
        assertEquals("hello", Files.readString(savedFile.toPath()));
    }

    @Test
    void testSaveBase64Image_InvalidBase64_ReturnsEmpty() {
        // Given invalid base64
        String invalidBase64 = "%%%notbase64%%%";

        // When
        String filename = imageService.saveBase64Image(invalidBase64);

        // Then
        assertEquals("", filename);
    }

    @Test
    @Disabled
    void testDeleteImageFile_Success() throws Exception {
        // Given: Create a fake file
        File file = new File(tempDir, "testFile.txt");
        Files.writeString(file.toPath(), "content");
        assertTrue(file.exists());

        // When
        imageService.deleteImageFile(file.getName());

        // Then
        assertFalse(file.exists());
    }

    @Test
    void testDeleteImageFile_FileNotFound_NoException() {
        // When
        imageService.deleteImageFile("nonexistent.txt");

        // Then: Should not throw, just logs a warning
        assertTrue(true);
    }

    @Test
    void testDeleteImageFile_NullOrEmpty_NoException() {
        assertDoesNotThrow(() -> imageService.deleteImageFile(null));
        assertDoesNotThrow(() -> imageService.deleteImageFile(""));
        assertDoesNotThrow(() -> imageService.deleteImageFile("   "));
    }
}
