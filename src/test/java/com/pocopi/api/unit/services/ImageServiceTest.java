package com.pocopi.api.unit.services;

import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.dto.config.Image;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.services.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final String DEFAULT_BASE_PATH = "/images";
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageConfig imageConfig;

    @Mock
    private MultipartFile multipartFile;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(imageRepository, imageConfig);
    }

    private void configureBasePath(String basePath) {
        when(imageConfig.getBasePath()).thenReturn(basePath);
    }

    private void configureBaseUrl(String baseUrl) {
        when(imageConfig.getBaseUrl()).thenReturn(baseUrl);
    }

    private void configureImageConfig(String basePath, String baseUrl) {
        configureBasePath(basePath);
        configureBaseUrl(baseUrl);
    }


    // ==================== getImageById Tests ====================

    @Test
    void getImageById_WithValidId_ShouldReturnImageDto() {
        // Arrange
        configureBaseUrl(DEFAULT_BASE_URL);

        int imageId = 1;
        ImageModel imageModel = ImageModel.builder()
            .id(imageId)
            .path("images/icon/20240101_120000_test.png")
            .alt("Test Alt")
            .build();

        when(imageRepository.findById(imageId))
            .thenReturn(Optional.of(imageModel));

        // Act
        Image result = imageService.getImageById(imageId);

        // Assert
        assertNotNull(result);
        assertEquals("http://localhost:8080/images/icon/20240101_120000_test.png", result.url());
        assertEquals("Test Alt", result.alt());
        verify(imageRepository, times(1)).findById(imageId);
    }

    @Test
    void getImageById_WithInvalidId_ShouldThrowException() {
        // Arrange
        int imageId = 999;
        when(imageRepository.findById(imageId))
            .thenReturn(Optional.empty());

        // Act
        Exception exception = null;
        try {
            imageService.getImageById(imageId);
        } catch (RuntimeException e) {
            exception = e;
        }

        // Assert
        assertNotNull(exception);
        assertInstanceOf(RuntimeException.class, exception);
        verify(imageRepository, times(1)).findById(imageId);

    }

    @Test
    void getImageById_WithNullAlt_ShouldReturnImageWithNullAlt() {
        configureBaseUrl(DEFAULT_BASE_URL);

        // Arrange
        int imageId = 2;
        ImageModel imageModel = ImageModel.builder()
            .id(imageId)
            .path("images/cards/20240101_120000_card.jpg")
            .alt(null)
            .build();

        when(imageRepository.findById(imageId))
            .thenReturn(Optional.of(imageModel));

        // Act
        Image result = imageService.getImageById(imageId);

        // Assert
        assertNotNull(result);
        assertNull(result.alt());
    }


    // ==================== deleteImageIfUnused Tests ====================

    @Test
    void deleteImageIfUnused_WithUsedImage_ShouldNotDeleteAnything() {
        // Arrange
        ImageModel imageModel = ImageModel.builder()
            .id(1)
            .path("images/icon/20240101_120000_test.png")
            .build();

        when(imageRepository.isImageUsed(1))
            .thenReturn(true);

        // Act
        imageService.deleteImageIfUnused(imageModel);

        // Assert
        verify(imageRepository, times(1)).isImageUsed(1);
        verify(imageRepository, never()).delete(any());
    }


    @Test
    void deleteImageIfUnused_WithUnusedImage_ShouldCallDelete() {
        configureBasePath(DEFAULT_BASE_PATH);

        // Arrange
        ImageModel imageModel = ImageModel.builder()
            .id(1)
            .path("images/icon/test.png")
            .build();

        when(imageRepository.isImageUsed(1))
            .thenReturn(false);

        try {
            imageService.deleteImageIfUnused(imageModel);
        } catch (HttpException e) {
            // Expected - el archivo no existe en /mock/path
        }

        // Assert
        verify(imageRepository, times(1)).isImageUsed(1);
        verify(imageRepository, times(1)).delete(imageModel);
    }


    // ==================== validateFileAndGetExtension Tests ====================

    @Test
    void validateFileAndGetExtension_WithNullFile_ShouldThrowBadRequest() {
        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> imageService.saveImageFile(ImageService.ImageCategory.ICON, null, "alt"));

        assertEquals("File cannot be empty", exception.getMessage());
    }

    @Test
    void validateFileAndGetExtension_WithEmptyFile_ShouldThrowBadRequest() throws IOException {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> imageService.saveImageFile(ImageService.ImageCategory.ICON, multipartFile, "alt"));

        assertEquals("File cannot be empty", exception.getMessage());
    }
}
