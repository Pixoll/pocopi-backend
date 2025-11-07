package com.pocopi.api.services;

import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.dto.config.Image;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.ImageRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ImageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private static final int MAX_FILE_SIZE = 1_000_000;
    private static final String MAX_FILE_SIZE_STR = MAX_FILE_SIZE / 1_000_000 + " MB";

    private static final List<String> SUPPORTED_IMAGE_TYPES = List.of("image/gif", "image/png", "image/jpeg");

    private static final Tika TIKA = new Tika();

    private final ImageRepository imageRepository;
    private final ImageConfig imageConfig;

    public ImageService(ImageRepository imageRepository, ImageConfig imageConfig) {
        this.imageRepository = imageRepository;
        this.imageConfig = imageConfig;
    }

    public ImageModel saveImageFile(ImageCategory category, MultipartFile file, String alt) {
        validateFile(file);

        final String relativePath = generateUniquePath(category.dir, file.getOriginalFilename());
        final Path fullPath = resolveFullPath(relativePath);

        try {
            createDirectoriesIfMissing(fullPath.getParent());
            Files.write(fullPath, file.getBytes());
        } catch (IOException e) {
            LOGGER.error("Could not save image file", e);
            throw HttpException.internalServerError("Could not save image file", e);
        }

        final ImageModel imageModel = new ImageModel();
        imageModel.setPath(relativePath);
        imageModel.setAlt(alt);

        return imageRepository.save(imageModel);
    }

    public void updateImageFile(ImageModel image, MultipartFile newFile) {
        validateFile(newFile);

        if (image.getId() < 1) {
            throw HttpException.badRequest("Cannot update an image with no id");
        }

        final Path fullPath = resolveFullPath(image.getPath());

        try {
            createDirectoriesIfMissing(fullPath.getParent());
            Files.write(fullPath, newFile.getBytes());
        } catch (IOException e) {
            LOGGER.error("Could not update image file", e);
            throw HttpException.internalServerError("Could not update image file", e);
        }
    }

    public void deleteImageIfUnused(ImageModel image) {
        if (imageRepository.isImageUsed(image.getId())) {
            return;
        }

        final Path fullPath = resolveFullPath(image.getPath());

        try {
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            LOGGER.error("Could not delete image file", e);
            throw HttpException.internalServerError("Could not delete image file", e);
        }

        imageRepository.delete(image);
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
        return filename != null ? filename.replaceAll("[^a-zA-Z0-9._-]", "_") : "file.jpg";
    }

    private String buildPublicUrl(String relativePath) {
        String normalizedPath = relativePath.replaceFirst("^/", "");

        if (!normalizedPath.startsWith("images/")) {
            normalizedPath = "images/" + normalizedPath;
        }

        final String baseUrl = imageConfig.getBaseUrl().replaceFirst("/$", "");

        return baseUrl + "/" + normalizedPath;
    }

    private Path resolveFullPath(String relativePath) {
        final String normalizedPath = relativePath.replaceFirst("^images/", "");
        return Paths.get(imageConfig.getBasePath(), normalizedPath);
    }

    private void createDirectoriesIfMissing(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw HttpException.badRequest("File cannot be empty");
        }

        try (final InputStream inputStream = file.getInputStream()) {
            final String mimeType = TIKA.detect(inputStream);

            if (mimeType == null || !SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
                throw HttpException.badRequest("File must be an image");
            }
        } catch (IOException e) {
            LOGGER.error("Could not analyze file", e);
            throw HttpException.internalServerError("Could not analyze file", e);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw HttpException.payloadTooLarge("File size cannot exceed " + MAX_FILE_SIZE_STR);
        }
    }

    public enum ImageCategory {
        ICON("icon"),
        INFO_CARD("cards"),
        FORM_QUESTION("forms/questions"),
        FORM_OPTION("forms/questions/options"),
        TEST_QUESTION("test/questions"),
        TEST_OPTION("test/questions/options");

        private final String dir;

        ImageCategory(String dir) {
            this.dir = dir;
        }
    }
}
