package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.models.ImageModel;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.services.interfaces.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImageServiceImp implements ImageService {

    ImageRepository imageRepository;

    private final String BASE_URL = "http://localhost:8081";

    public ImageServiceImp(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public UploadImageResponse saveImageToExistsUrl(MultipartFile file, String path) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            ImageModel imageModel = imageRepository.findByPath(path);
            if (imageModel == null) {
                throw new RuntimeException("Image path not found in database: " + path);
            }


            Path uploadDir = Paths.get("images");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String relativePath = path.replaceFirst("^images/", "");
            Path fullPath = uploadDir.resolve(relativePath);
            if (!Files.exists(fullPath.getParent())) {
                Files.createDirectories(fullPath.getParent());
            }

            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

            String url = BASE_URL + "/" + path;
            return new UploadImageResponse(url);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

}
