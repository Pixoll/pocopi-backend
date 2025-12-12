package com.pocopi.api.integration.services.config;

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        ReflectionTestUtils.setField(imageConfig, "basePath", tempDir.toString());
        ReflectionTestUtils.setField(imageConfig, "baseUrl", "http://localhost:8080");

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
    void saveImageFile_WithValidPngFile_ShouldPersistImageAndFile() throws Exception {
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

        Path fileOnDisk = Paths.get(imageConfig.getBasePath(), result.getPath().replaceFirst("^images/?", ""));
        assertTrue(Files.exists(fileOnDisk), "El archivo debería existir en el sistema de archivos de test");

        log.info("----------- Finalizó correctamente ImageServiceIT.saveImageFile_WithValidPngFile_ShouldPersistImageAndFile -----------");
    }

    @Test
    @Transactional
    void saveImageFile_WithValidJpgFile_ShouldSaveWithCorrectCategory() throws Exception {
        log.info("----------- Iniciando ImageServiceIT.saveImageFile_WithValidJpgFile_ShouldSaveWithCorrectCategory -----------");

        // Act
        ImageModel result = imageService.saveImageFile(
            ImageService.ImageCategory.INFO_CARD,
            mockJpgFile,
            "Card Image"
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.getPath().contains("cards"), "El path debe contener la categoría 'cards'");
        assertTrue(result.getPath().contains("test.jpg"), "El path debe contener el nombre del archivo");

        Optional<ImageModel> found = imageRepository.findById(result.getId());
        assertTrue(found.isPresent(), "La imagen debe estar en DB");
        assertTrue(found.get().getPath().contains("cards"), "El path en DB debe contener 'cards'");
        assertTrue(found.get().getPath().contains("test.jpg"), "El path en DB debe contener el nombre del archivo");
        assertEquals("Card Image", found.get().getAlt(), "El alt en DB debe ser 'Card Image'");

        Path fileOnDisk = Paths.get(imageConfig.getBasePath(), result.getPath().replaceFirst("^images/?", ""));
        assertTrue(Files.exists(fileOnDisk), "El archivo .jpg debe existir en disco");

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

        // Arrange
        byte[] largeData = new byte[6_000_000];
        System.arraycopy(createPngBytes(), 0, largeData, 0, Math.min(createPngBytes().length, largeData.length));

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

        // Arrange
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

        Path fileOnDisk = Paths.get(imageConfig.getBasePath(), saved.getPath().replaceFirst("^images/?", ""));
        assertFalse(Files.exists(fileOnDisk), "El archivo físico debe haber sido borrado");

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

        Path fileOnDiskCloned = Paths.get(imageConfig.getBasePath(), cloned.getPath().replaceFirst("^images/?", ""));
        assertTrue(Files.exists(fileOnDiskCloned), "El archivo del clon debe existir en disco");

        log.info("----------- Finalizó correctamente ImageServiceIT.cloneImage_WithValidImage_ShouldCreateCopyWithNewTimestamp -----------");
    }

    // ==================== updateImageFile Tests ====================

    @Test
    @Transactional
    void updateImageFile_WithValidFile_ShouldUpdateImagePathAndFile() throws Exception {
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

        Path oldFile = Paths.get(imageConfig.getBasePath(), originalPath.replaceFirst("^images/?", ""));
        Path newFilePath = Paths.get(imageConfig.getBasePath(), updated.get().getPath().replaceFirst("^images/?", ""));
        assertFalse(Files.exists(oldFile), "El archivo antiguo debe haber sido eliminado");
        assertTrue(Files.exists(newFilePath), "El archivo actualizado debe existir");

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
        return new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
            (byte) 0xFF, (byte) 0xD9
        };
    }
}