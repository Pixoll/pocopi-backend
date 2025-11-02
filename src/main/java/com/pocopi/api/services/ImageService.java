package com.pocopi.api.services;

import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.dto.image.Image;
import com.pocopi.api.dto.image.ImageUrl;
import com.pocopi.api.models.image.ImageModel;
import com.pocopi.api.repositories.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageConfig imageConfig;

    public ImageService(ImageRepository imageRepository, ImageConfig imageConfig) {
        this.imageRepository = imageRepository;
        this.imageConfig = imageConfig;
    }

    public void saveImageBytes(byte[] imageBytes, String relativePath) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be empty");
        }
        try {
            final ImageModel imageModel = imageRepository.findByPath(relativePath);
            if (imageModel == null) {
                throw new RuntimeException("Image path not found in database: " + relativePath);
            }

            final Path fullPath = resolveFullPath(relativePath);
            ensureDirectoryExists(fullPath.getParent());

            Files.write(fullPath, imageBytes);

            final String publicUrl = buildPublicUrl(relativePath);

            new ImageUrl(publicUrl);
        } catch (IOException e) {
            throw new RuntimeException("Error saving image bytes: " + e.getMessage(), e);
        }
    }

    public ImageUrl createAndSaveImageBytes(
        byte[] imageBytes,
        String category,
        String originalFilename,
        String alt
    ) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be empty");
        }

        try {
            final String relativePath = generateUniquePath(category, originalFilename);

            final Path fullPath = resolveFullPath(relativePath);
            ensureDirectoryExists(fullPath.getParent());
            Files.write(fullPath, imageBytes);

            final ImageModel imageModel = new ImageModel();
            imageModel.setPath(relativePath);
            imageModel.setAlt(alt);
            imageRepository.save(imageModel);

            final String publicUrl = buildPublicUrl(relativePath);

            return new ImageUrl(publicUrl);
        } catch (IOException e) {
            throw new RuntimeException("Error creating image from bytes: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String relativePath) {
        try {
            final Path fullPath = resolveFullPath(relativePath);
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting image: " + e.getMessage(), e);
        }
    }

    public Image getImageByPath(String relativePath) {
        final ImageModel imageModel = imageRepository.findByPath(relativePath);
        if (imageModel == null) {
            throw new RuntimeException("Image not found with path: " + relativePath);
        }
        final String url = buildPublicUrl(relativePath);
        return new Image(url, imageModel.getAlt());
    }

    public ImageModel getImageModelByPath(String path) {
        return imageRepository.findByPath(path);
    }

    public Image getImageById(int id) {
        final ImageModel imageModel = imageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));

        final String url = buildPublicUrl(imageModel.getPath());
        return new Image(url, imageModel.getAlt());
    }

    private String generateUniquePath(String category, String originalFilename) {
        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        final String sanitizedFilename = sanitizeFilename(originalFilename);

        return String.format("images/%s/%s_%s", category, timestamp, sanitizedFilename);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "file.jpg";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String buildPublicUrl(String relativePath) {
        String normalizedPath = relativePath.replaceFirst("^/", "");

        if (!normalizedPath.startsWith("images/")) {
            normalizedPath = "images/" + normalizedPath;
        }

        String baseUrl = imageConfig.getBaseUrl();
        baseUrl = baseUrl.replaceFirst("/$", "");

        return baseUrl + "/" + normalizedPath;
    }

    private Path resolveFullPath(String relativePath) {
        final String normalizedPath = relativePath.replaceFirst("^images/", "");
        return Paths.get(imageConfig.getBasePath(), normalizedPath);
    }

    private void ensureDirectoryExists(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        final String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 5MB");
        }
    }
}
