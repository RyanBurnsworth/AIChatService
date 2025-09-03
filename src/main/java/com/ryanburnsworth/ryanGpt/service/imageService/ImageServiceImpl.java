package com.ryanburnsworth.ryanGpt.service.imageService;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

import static com.ryanburnsworth.ryanGpt.utils.Constants.FILENAME;
import static com.ryanburnsworth.ryanGpt.utils.Constants.FILE_DIRECTORY;

@Service
public class ImageServiceImpl implements ImageService {
    @Override
    public void saveBase64Image(String base64Image) {
        try {

            // Remove spaces, newlines, tabs
            base64Image = base64Image.replaceAll("\\s+", "");

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            String staticDir = new File(FILE_DIRECTORY).getAbsolutePath();

            File dir = new File(staticDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // TODO: use unique filename to avoid conflicts
            String filePath = staticDir + File.separator + FILENAME;

            try (OutputStream out = new FileOutputStream(filePath)) {
                out.write(imageBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Create a deleteFile method for removing files after use
}
