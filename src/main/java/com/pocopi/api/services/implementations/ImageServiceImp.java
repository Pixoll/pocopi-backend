package com.pocopi.api.services.implementations;

import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.dto.Image.Image;
import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.models.image.ImageModel;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.services.interfaces.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ImageServiceImp implements ImageService {

    ImageRepository imageRepository;
    private final ImageConfig imageConfig;

    public ImageServiceImp(ImageRepository imageRepository, ImageConfig imageConfig) {
        this.imageRepository = imageRepository;
        this.imageConfig = imageConfig;
    }

    @Override
    public UploadImageResponse saveImageToExistsUrl(MultipartFile file, String relativePath) {
        validateFile(file);

        ImageModel imageModel = imageRepository.findByPath(relativePath);
        if (imageModel == null) {
            throw new RuntimeException("Image path not found in database: " + relativePath);
        }

        saveFileToFileSystem(file, relativePath);

        String publicUrl = buildPublicUrl(relativePath);

        return new UploadImageResponse(publicUrl);
    }


    @Override
    public void saveImageBytes(byte[] imageBytes, String relativePath) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be empty");
        }
        try {
            ImageModel imageModel = imageRepository.findByPath(relativePath);
            if (imageModel == null) {
                throw new RuntimeException("Image path not found in database: " + relativePath);
            }

            Path fullPath = resolveFullPath(relativePath);
            ensureDirectoryExists(fullPath.getParent());

            Files.write(fullPath, imageBytes);

            String publicUrl = buildPublicUrl(relativePath);

            new UploadImageResponse(publicUrl);

        } catch (IOException e) {
            throw new RuntimeException("Error saving image bytes: " + e.getMessage(), e);
        }
    }
    @Override
    public UploadImageResponse createAndSaveImageBytes(byte[] imageBytes, String category, String originalFilename, String alt) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be empty");
        }

        try {
            String relativePath = generateUniquePath(category, originalFilename);

            Path fullPath = resolveFullPath(relativePath);
            ensureDirectoryExists(fullPath.getParent());
            Files.write(fullPath, imageBytes);

            ImageModel imageModel = new ImageModel();
            imageModel.setPath(relativePath);
            imageModel.setAlt(alt);
            imageRepository.save(imageModel);

            String publicUrl = buildPublicUrl(relativePath);

            return new UploadImageResponse(publicUrl);

        } catch (IOException e) {
            throw new RuntimeException("Error creating image from bytes: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String relativePath) {
        try {
            Path fullPath = resolveFullPath(relativePath);
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting image: " + e.getMessage(), e);
        }
    }

    @Override
    public Image getImageByPath(String relativePath) {
        ImageModel imageModel = imageRepository.findByPath(relativePath);
        if (imageModel == null) {
            throw new RuntimeException("Image not found with path: " + relativePath);
        }
        String url = buildPublicUrl(relativePath);
        return new Image(url, imageModel.getAlt());
    }

    @Override
    public ImageModel getImageModelByPath(String path) {
        return imageRepository.findByPath(path);
    }

    @Override
    public Image getImageById(int id) {
        ImageModel imageModel = imageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Image not found with id: " + id));

        String url = buildPublicUrl(imageModel.getPath());
        return new Image(url, imageModel.getAlt());
    }

    private void saveFileToFileSystem(MultipartFile file, String relativePath) {
        try {
            Path fullPath = resolveFullPath(relativePath);
            ensureDirectoryExists(fullPath.getParent());

            Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Error saving file: " + e.getMessage(), e);
        }
    }

    private String generateUniquePath(String category, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedFilename = sanitizeFilename(originalFilename);

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
        String normalizedPath = relativePath.replaceFirst("^images/", "");
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

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 5MB");
        }
    }
}
