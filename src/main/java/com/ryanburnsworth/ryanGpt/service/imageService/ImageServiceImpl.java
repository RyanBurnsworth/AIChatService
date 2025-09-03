package com.ryanburnsworth.ryanGpt.service.imageService;

import com.ryanburnsworth.ryanGpt.utils.Constants;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

@Service
public class ImageServiceImpl implements ImageService {
    public ImageServiceImpl() {
    }

    @Override
    public void saveBase64Image(String base64Image) {
        try {

            // Remove spaces, newlines, tabs
            base64Image = base64Image.replaceAll("\\s+", "");

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            String staticDir = new File("src/main/resources/static").getAbsolutePath();

            File dir = new File(staticDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = staticDir + File.separator + Constants.FILENAME;

            try (OutputStream out = new FileOutputStream(filePath)) {
                out.write(imageBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
