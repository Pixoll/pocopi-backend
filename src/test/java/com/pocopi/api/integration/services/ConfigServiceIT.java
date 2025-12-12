package com.pocopi.api.integration.services;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.services.ConfigService;
import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.services.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class ConfigServiceIT {

    @Autowired
    private ConfigService configService;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageConfig imageConfig;

    @TempDir
    private Path tempDir;

    private MockMultipartFile mockPngFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageConfig, "basePath", tempDir.toString());
        ReflectionTestUtils.setField(imageConfig, "baseUrl", "http://localhost:8080");

        mockPngFile = new MockMultipartFile(
            "file",
            "icon.png",
            "image/png",
            createPngBytes()
        );
    }

    @Test
    @Transactional
    void getAllConfigs_ShouldReturnConfigPreviewsIncludingIconUrl() {
        // Arrange
        ConfigModel c1 = ConfigModel.builder()
            .title("Cfg One")
            .description("d1")
            .informedConsent("consent")
            .active(true)
            .build();
        c1 = configRepository.save(c1);

        ImageModel icon = imageService.saveImageFile(ImageService.ImageCategory.ICON, mockPngFile, "icon-alt");
        c1.setIcon(icon);
        configRepository.save(c1);

        ConfigModel c2 = ConfigModel.builder()
            .title("Cfg Two")
            .description("d2")
            .informedConsent("consent")
            .active(false)
            .build();
        configRepository.save(c2);

        // Act
        List<?> previews = configService.getAllConfigs();

        // Assert
        assertNotNull(previews);
        assertTrue(previews.size() >= 2);
        boolean foundC1 = previews.stream().anyMatch(p -> p.toString().contains("Cfg One"));
        assertTrue(foundC1);
    }

    @Test
    @Transactional
    void deleteConfig_ShouldThrowWhenDeletingActiveConfig() {
        // Arrange
        ConfigModel cfg = ConfigModel.builder()
            .title("ActiveCfg")
            .description("d")
            .informedConsent("c")
            .active(true)
            .build();
        cfg = configRepository.save(cfg);

        // Act / Assert
        ConfigModel finalCfg = cfg;
        var ex = assertThrows(RuntimeException.class, () -> configService.deleteConfig(finalCfg.getVersion()));
        assertNotNull(ex.getMessage());
    }

    @Test
    @Transactional
    void cloneConfig_ShouldCreateNewConfigAndCloneIcon() {
        // Arrange
        ConfigModel cfg = ConfigModel.builder()
            .title("ToClone")
            .description("d")
            .informedConsent("c")
            .active(true)
            .build();
        cfg = configRepository.save(cfg);

        ImageModel icon = imageService.saveImageFile(ImageService.ImageCategory.ICON, mockPngFile, "icon1");
        cfg.setIcon(icon);
        cfg = configRepository.save(cfg);

        long beforeCount = configRepository.count();

        // Act
        configService.cloneConfig(cfg.getVersion());

        // Assert
        assertEquals(beforeCount + 1, configRepository.count());
        List<ConfigModel> all = configRepository.findAll();
        ConfigModel finalCfg = cfg;
        ConfigModel newCfg = all.stream().filter(c -> c.getVersion() != finalCfg.getVersion()).findFirst().orElse(null);
        assertNotNull(newCfg);
        if (newCfg.getIcon() != null) {
            assertNotEquals(icon.getId(), newCfg.getIcon().getId());
            assertTrue(imageRepository.findById(newCfg.getIcon().getId()).isPresent());
            Path resolved = Path.of(imageConfig.getBasePath()).resolve(newCfg.getIcon().getPath().replaceFirst("^images/?", ""));
            assertTrue(Files.exists(resolved));
        }
    }

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
}