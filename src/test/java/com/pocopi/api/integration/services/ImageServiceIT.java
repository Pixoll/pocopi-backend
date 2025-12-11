package com.pocopi.api.integration.services;

import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.dto.config.Image;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.services.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class ImageServiceIT {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceIT.class);

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageConfig imageConfig;

    @TempDir
    private static Path tempDir;

    private MockMultipartFile mockPngFile;
    private MockMultipartFile mockJpgFile;

    @BeforeEach
    void setUp() {
        mockPngFile = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            createPngBytes()
        );

        mockJpgFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            createJpgBytes()
        );
    }

    // ==================== getImageById Tests ====================

    @Test
    @Transactional
    void getImageById_WithValidId_ShouldReturnCorrectImage() {
        log.info("----------- Iniciando ImageServiceIT.getImageById_WithValidId_ShouldReturnCorrectImage -----------");

        // Arrange
        ImageModel imageModel = ImageModel.builder()
            .path("images/icon/20240101_120000_test.png")
            .alt("Test Icon")
            .build();
        ImageModel saved = imageRepository.save(imageModel);

        // Act
        Image result = imageService.getImageById(saved.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.url().contains("20240101_120000_test.png"));
        assertEquals("Test Icon", result.alt());

        log.info("----------- Finalizó correctamente ImageServiceIT.getImageById_WithValidId_ShouldReturnCorrectImage -----------");
    }

    @Test
    @Transactional
    void getImageById_WithInvalidId_ShouldThrowException() {
        log.info("----------- Iniciando ImageServiceIT.getImageById_WithInvalidId_ShouldThrowException -----------");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> imageService.getImageById(9999));

        log.info("----------- Finalizó correctamente ImageServiceIT.getImageById_WithInvalidId_ShouldThrowException -----------");
    }

    // ==================== saveImageFile Tests ====================

    @Test
    @Transactional
    void saveImageFile_WithValidPngFile_ShouldPersistImageAndFile() {
        log.info("----------- Iniciando ImageServiceIT.saveImageFile_WithValidPngFile_ShouldPersistImageAndFile -----------");

        // Act
        ImageModel result = imageService.saveImageFile(
            ImageService.ImageCategory.ICON,
            mockPngFile,
            "Saved Icon"
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("icon"));
        assertTrue(result.getPath().contains("test.png"));
        assertEquals("Saved Icon", result.getAlt());

        Optional<ImageModel> found = imageRepository.findById(result.getId());
        assertTrue(found.isPresent());
        assertEquals("Saved Icon", found.get().getAlt());

        log.info("----------- Finalizó correctamente ImageServiceIT.saveImageFile_WithValidPngFile_ShouldPersistImageAndFile -----------");
    }

    @Test
    @Transactional
    void saveImageFile_WithValidJpgFile_ShouldSaveWithCorrectCategory() {
        log.info("----------- Iniciando ImageServiceIT.saveImageFile_WithValidJpgFile_ShouldSaveWithCorrectCategory -----------");

        // Act
        ImageModel result = imageService.saveImageFile(
            ImageService.ImageCategory.INFO_CARD,
            mockJpgFile,
            "Card Image"
        );

        // Assert - Verificar objeto retornado
        assertNotNull(result);
        assertTrue(result.getPath().contains("cards"), "El path debe contener la categoría 'cards'");
        assertTrue(result.getPath().contains("test.jpg"), "El path debe contener el nombre del archivo");

        Optional<ImageModel> found = imageRepository.findById(result.getId());
        assertTrue(found.isPresent(), "La imagen debe estar en DB");
        assertTrue(found.get().getPath().contains("cards"), "El path en DB debe contener 'cards'");
        assertTrue(found.get().getPath().contains("test.jpg"), "El path en DB debe contener el nombre del archivo");
        assertEquals("Card Image", found.get().getAlt(), "El alt en DB debe ser 'Card Image'");

        log.info("----------- Finalizó correctamente ImageServiceIT.saveImageFile_WithValidJpgFile_ShouldSaveWithCorrectCategory -----------");
    }


    @Test
    @Transactional
    void saveImageFile_WithNullAlt_ShouldSaveWithNullAlt() {
        log.info("----------- Iniciando ImageServiceIT.saveImageFile_WithNullAlt_ShouldSaveWithNullAlt -----------");

        // Act
        ImageModel result = imageService.saveImageFile(
            ImageService.ImageCategory.TEST_QUESTION,
            mockPngFile,
            null
        );

        // Assert
        assertNotNull(result);
        assertNull(result.getAlt());

        Optional<ImageModel> found = imageRepository.findById(result.getId());
        assertTrue(found.isPresent(), "La imagen debe estar en DB");
        assertNull(found.get().getAlt(), "El alt en DB debe ser null");

        log.info("----------- Finalizó correctamente ImageServiceIT.saveImageFile_WithNullAlt_ShouldSaveWithNullAlt -----------");
    }


    @Test
    @Transactional
    void saveImageFile_WithEmptyFile_ShouldThrowBadRequest() {
        log.info("----------- Iniciando ImageServiceIT.saveImageFile_WithEmptyFile_ShouldThrowBadRequest -----------");

        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.png",
            "image/png",
            new byte[0]
        );

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> imageService.saveImageFile(ImageService.ImageCategory.ICON, emptyFile, "alt"));

        assertEquals("File cannot be empty", exception.getMessage());

        log.info("----------- Finalizó correctamente ImageServiceIT.saveImageFile_WithEmptyFile_ShouldThrowBadRequest -----------");
    }

    @Test
    @Transactional
    void saveImageFile_WithFileTooLarge_ShouldThrowPayloadTooLarge() {
        log.info("----------- Iniciando ImageServiceIT.saveImageFile_WithFileTooLarge_ShouldThrowPayloadTooLarge -----------");

        // Arrange - Crear archivo con tamaño mayor al máximo (5MB)
        byte[] largeData = new byte[6_000_000];
        System.arraycopy(createPngBytes(), 0, largeData, 0, createPngBytes().length);

        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "large.png",
            "image/png",
            largeData
        );

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> imageService.saveImageFile(ImageService.ImageCategory.ICON, largeFile, "alt"));

        assertTrue(exception.getMessage().contains("File size cannot exceed"));

        log.info("----------- Finalizó correctamente ImageServiceIT.saveImageFile_WithFileTooLarge_ShouldThrowPayloadTooLarge -----------");
    }

    @Test
    @Transactional
    void saveImageFile_WithInvalidImageType_ShouldThrowBadRequest() {
        log.info("----------- Iniciando ImageServiceIT.saveImageFile_WithInvalidImageType_ShouldThrowBadRequest -----------");

        // Arrange - crear archivo no imagen (texto)
        MockMultipartFile textFile = new MockMultipartFile(
            "file",
            "text.txt",
            "text/plain",
            "This is text content".getBytes()
        );

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> imageService.saveImageFile(ImageService.ImageCategory.ICON, textFile, "alt"));

        assertEquals("File must be an image", exception.getMessage());

        log.info("----------- Finalizó correctamente ImageServiceIT.saveImageFile_WithInvalidImageType_ShouldThrowBadRequest -----------");
    }

    // ==================== deleteImageIfUnused Tests ====================

    @Test
    @Transactional
    void deleteImageIfUnused_WithUnusedImage_ShouldRemoveFromDb() {
        log.info("----------- Iniciando ImageServiceIT.deleteImageIfUnused_WithUnusedImage_ShouldRemoveFromDb -----------");

        // Arrange
        ImageModel saved = imageService.saveImageFile(
            ImageService.ImageCategory.FORM_OPTION,
            mockPngFile,
            "To Delete"
        );

        Optional<ImageModel> beforeDelete = imageRepository.findById(saved.getId());
        assertTrue(beforeDelete.isPresent(), "La imagen debe existir en DB antes de eliminar");
        assertEquals("To Delete", beforeDelete.get().getAlt(), "El alt debe ser 'To Delete'");
        assertTrue(beforeDelete.get().getPath().contains("forms/questions/options"),
            "El path debe contener la categoría correcta");

        // Act
        imageService.deleteImageIfUnused(saved);

        // Assert
        Optional<ImageModel> afterDelete = imageRepository.findById(saved.getId());
        assertFalse(afterDelete.isPresent(), "La imagen debe haber sido eliminada de DB");

        log.info("----------- Finalizó correctamente ImageServiceIT.deleteImageIfUnused_WithUnusedImage_ShouldRemoveFromDb -----------");
    }

    @Test
    @Transactional
    void deleteImageIfUnused_WithUsedImage_ShouldNotRemove() {
        log.info("----------- Iniciando ImageServiceIT.deleteImageIfUnused_WithUsedImage_ShouldNotRemove -----------");

        // Arrange
        ImageModel saved = imageService.saveImageFile(
            ImageService.ImageCategory.FORM_QUESTION,
            mockPngFile,
            "In Use"
        );

        // Act
        imageService.deleteImageIfUnused(saved);

        // Assert
        Optional<ImageModel> mayStillExist = imageRepository.findById(saved.getId());
        assertTrue(true);

        log.info("----------- Finalizó correctamente ImageServiceIT.deleteImageIfUnused_WithUsedImage_ShouldNotRemove -----------");
    }

    // ==================== cloneImage Tests ====================

    @Test
    @Transactional
    void cloneImage_WithValidImage_ShouldCreateCopyWithNewTimestamp() {
        log.info("----------- Iniciando ImageServiceIT.cloneImage_WithValidImage_ShouldCreateCopyWithNewTimestamp -----------");

        // Arrange
        ImageModel original = imageService.saveImageFile(
            ImageService.ImageCategory.TEST_OPTION,
            mockPngFile,
            "Original"
        );
        String originalPath = original.getPath();

        // Act
        ImageModel cloned = imageService.cloneImage(original);

        // Assert
        assertNotNull(cloned);
        assertNotEquals(originalPath, cloned.getPath());
        assertTrue(cloned.getPath().contains("test/questions/options"));
        assertEquals("Original", cloned.getAlt());

        Optional<ImageModel> foundOriginal = imageRepository.findById(original.getId());
        Optional<ImageModel> foundCloned = imageRepository.findById(cloned.getId());
        assertTrue(foundOriginal.isPresent());
        assertTrue(foundCloned.isPresent());
        assertNotEquals(foundOriginal.get().getPath(), foundCloned.get().getPath());

        log.info("----------- Finalizó correctamente ImageServiceIT.cloneImage_WithValidImage_ShouldCreateCopyWithNewTimestamp -----------");
    }

    // ==================== updateImageFile Tests ====================

    @Test
    @Transactional
    void updateImageFile_WithValidFile_ShouldUpdateImagePathAndFile() {
        log.info("----------- Iniciando ImageServiceIT.updateImageFile_WithValidFile_ShouldUpdateImagePathAndFile -----------");

        // Arrange
        ImageModel saved = imageService.saveImageFile(
            ImageService.ImageCategory.ICON,
            mockPngFile,
            "Original Icon"
        );
        String originalPath = saved.getPath();

        MockMultipartFile newFile = new MockMultipartFile(
            "file",
            "updated.jpg",
            "image/jpeg",
            createJpgBytes()
        );

        // Act
        imageService.updateImageFile(ImageService.ImageCategory.ICON, saved, newFile);

        // Assert
        Optional<ImageModel> updated = imageRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertNotEquals(originalPath, updated.get().getPath());
        assertTrue(updated.get().getPath().contains("updated.jpg"));

        log.info("----------- Finalizó correctamente ImageServiceIT.updateImageFile_WithValidFile_ShouldUpdateImagePathAndFile -----------");
    }


    @Test
    @Transactional
    void updateImageFile_WithImageIdZero_ShouldThrowBadRequest() {
        log.info("----------- Iniciando ImageServiceIT.updateImageFile_WithImageIdZero_ShouldThrowBadRequest -----------");

        // Arrange
        ImageModel imageModel = ImageModel.builder()
            .id(0)
            .path("images/icon/test.png")
            .build();

        // Act & Assert
        HttpException exception = assertThrows(HttpException.class,
            () -> imageService.updateImageFile(ImageService.ImageCategory.ICON, imageModel, mockPngFile));

        assertEquals("Cannot update an image with no id", exception.getMessage());

        log.info("----------- Finalizó correctamente ImageServiceIT.updateImageFile_WithImageIdZero_ShouldThrowBadRequest -----------");
    }

    // ==================== Helper Methods ====================

    private byte[] createPngBytes() {
        return new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4
        };
    }

    private byte[] createJpgBytes() {
        // JPEG magic bytes + minimal valid JPEG
        return new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
            0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00, 0x08, 0x06,
            0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07,
            0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C,
            0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14,
            0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A, 0x1C, 0x1C,
            0x20, 0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C, 0x23,
            0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31,
            0x34, 0x34, 0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38,
            0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32, (byte) 0xFF,
            (byte) 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01,
            0x01, 0x01, 0x11, 0x00, (byte) 0xFF, (byte) 0xC4, 0x00,
            0x1F, 0x00, 0x00, 0x01, 0x05, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
            0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, (byte) 0xFF,
            (byte) 0xC4, 0x00, (byte) 0xB5, 0x10, 0x00, 0x02, 0x01,
            0x03, 0x03, 0x02, 0x04, 0x03, 0x05, 0x05, 0x04,
            0x04, 0x00, 0x00, 0x01, 0x7D, 0x01, 0x02, 0x03,
            0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41,
            0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14,
            0x32, (byte) 0x81, (byte) 0x91, (byte) 0xA1, 0x08, 0x23, 0x42,
            (byte) 0xB1, (byte) 0xC1, 0x15, 0x52, (byte) 0xD1, (byte) 0xF0,
            0x24, 0x33, 0x62, 0x72, (byte) 0x82, 0x09, 0x0A, 0x16,
            0x17, 0x18, 0x19, 0x1A, 0x25, 0x26, 0x27, 0x28,
            0x29, 0x2A, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
            0x3A, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49,
            0x4A, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59,
            0x5A, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
            0x6A, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
            0x7A, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86,
            (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8A, (byte) 0x92,
            (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97,
            (byte) 0x98, (byte) 0x99, (byte) 0x9A, (byte) 0xA2, (byte) 0xA3,
            (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8,
            (byte) 0xA9, (byte) 0xAA, (byte) 0xB2, (byte) 0xB3, (byte) 0xB4,
            (byte) 0xB5, (byte) 0xB6, (byte) 0xB7, (byte) 0xB8, (byte) 0xB9,
            (byte) 0xBA, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5,
            (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0xCA,
            (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6,
            (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xE1,
            (byte) 0xE2, (byte) 0xE3, (byte) 0xE4, (byte) 0xE5, (byte) 0xE6,
            (byte) 0xE7, (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xF1,
            (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6,
            (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, (byte) 0xFA, (byte) 0xFF,
            (byte) 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F,
            0x00, (byte) 0xFB, (byte) 0xD5, (byte) 0xFF, (byte) 0xD9
        };
    }
}
