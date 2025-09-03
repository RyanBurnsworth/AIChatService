package com.ryanburnsworth.ryanGpt.service.imageService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.UUID;

import static com.ryanburnsworth.ryanGpt.utils.Constants.FILE_DIRECTORY;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    @Override
    public String saveBase64Image(String base64Image) {
        try {

            // Remove spaces, newlines, tabs
            base64Image = base64Image.replaceAll("\\s+", "");

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            String staticDir = new File(FILE_DIRECTORY).getAbsolutePath();

            File dir = new File(staticDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filename = String.valueOf(UUID.randomUUID());
            String filePath = staticDir + File.separator + filename;

            try (OutputStream out = new FileOutputStream(filePath)) {
                out.write(imageBytes);
            }

            return filename;
        } catch (Exception e) {
            log.error("Error saving image from base64: {}", e.getMessage());
        }

        return "";
    }

    @Override
    public void deleteImageFile(String filename) {
        if (filename == null || filename.isBlank()) {
            log.warn("Filename is null or empty, nothing to delete.");
            return;
        }

        try {
            String staticDir = new File(FILE_DIRECTORY).getAbsolutePath();
            File file = new File(staticDir, filename);

            if (file.exists()) {
                if (file.delete()) {
                    log.info("Successfully deleted image file: {}", file.getAbsolutePath());
                } else {
                    log.error("Failed to delete image file: {}", file.getAbsolutePath());
                }
            } else {
                log.warn("File not found, cannot delete: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error deleting image file {}: {}", filename, e.getMessage(), e);
        }
    }
}
