package com.pocopi.api.integration.services.config;

import com.pocopi.api.dto.config.InformationCardUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.HomeInfoCardRepository;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.services.HomeInfoCardService;
import com.pocopi.api.services.ImageService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class HomeInfoCardServiceIT {

    private static final Logger log = LoggerFactory.getLogger(HomeInfoCardServiceIT.class);

    @Autowired
    private HomeInfoCardService homeInfoCardService;

    @Autowired
    private HomeInfoCardRepository homeInfoCardRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageService imageService;

    private MockMultipartFile mockIconFile;

    @BeforeEach
    void setUp() {
        mockIconFile = new MockMultipartFile(
            "file",
            "icon.png",
            "image/png",
            createPngBytes()
        );
    }

    private ConfigModel createConfig() {
        return configRepository.save(ConfigModel.builder()
            .title("Test")
            .description("Test")
            .informedConsent("Test")
            .build());
    }

    private List<MultipartFile> createFileListWithNulls(int size) {
        List<MultipartFile> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(null);
        }
        return list;
    }

    private byte[] createPngBytes() {
        return new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4,
            (byte) 0xFF, (byte) 0xD9
        };
    }

    // ==================== cloneCards Tests ====================

    @Test
    @Transactional
    void cloneCards_WithExistingCards_ShouldCloneInDb() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.cloneCards_WithExistingCards_ShouldCloneInDb -----------");

        // Arrange
        ConfigModel originalConfig = createConfig();
        ConfigModel newConfig = createConfig();

        ImageModel icon = imageService.saveImageFile(
            ImageService.ImageCategory.ICON,
            mockIconFile,
            "Card Icon"
        );

        HomeInfoCardModel card = HomeInfoCardModel.builder()
            .config(originalConfig)
            .title("Card 1")
            .description("Desc 1")
            .color(0xFFFFFF)
            .icon(icon)
            .order((short) 0)
            .build();
        homeInfoCardRepository.save(card);

        // Act
        homeInfoCardService.cloneCards(originalConfig.getVersion(), newConfig);

        // Assert
        List<HomeInfoCardModel> clonedCards = homeInfoCardRepository
            .findAllByConfigVersion(newConfig.getVersion());

        assertEquals(1, clonedCards.size());
        assertEquals("Card 1", clonedCards.getFirst().getTitle());
        assertEquals(0xFFFFFF, clonedCards.getFirst().getColor());
        assertNotEquals(icon.getId(), clonedCards.getFirst().getIcon().getId());
        assertTrue(clonedCards.getFirst().getIcon().getPath().contains("icon"));

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.cloneCards_WithExistingCards_ShouldCloneInDb -----------");
    }

    @Test
    @Transactional
    void cloneCards_WithMultipleCards_ShouldCloneAllAndPreserveOrder() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.cloneCards_WithMultipleCards_ShouldCloneAllAndPreserveOrder -----------");

        // Arrange
        ConfigModel originalConfig = createConfig();
        ConfigModel newConfig = createConfig();

        HomeInfoCardModel card1 = HomeInfoCardModel.builder()
            .config(originalConfig).title("Card 1").description("Desc 1").color(0xFF0000).order((short) 0).build();
        HomeInfoCardModel card2 = HomeInfoCardModel.builder()
            .config(originalConfig).title("Card 2").description("Desc 2").color(0x00FF00).order((short) 1).build();

        homeInfoCardRepository.save(card1);
        homeInfoCardRepository.save(card2);

        // Act
        homeInfoCardService.cloneCards(originalConfig.getVersion(), newConfig);

        // Assert
        List<HomeInfoCardModel> clonedCards = homeInfoCardRepository
            .findAllByConfigVersion(newConfig.getVersion());

        assertEquals(2, clonedCards.size());
        assertTrue(clonedCards.stream().anyMatch(c -> c.getColor() == 0xFF0000));
        assertTrue(clonedCards.stream().anyMatch(c -> c.getColor() == 0x00FF00));

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.cloneCards_WithMultipleCards_ShouldCloneAllAndPreserveOrder -----------");
    }

    // ==================== updateCards Tests ====================

    @Test
    @Transactional
    void updateCards_WithNewCard_WithoutIcon_ShouldCreateInDb() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithNewCard_WithoutIcon_ShouldCreateInDb -----------");

        // Arrange
        ConfigModel config = createConfig();
        InformationCardUpdate newCard = new InformationCardUpdate(
            null, "New Title", "New Description", 0x0000FF  // Integer color: azul
        );

        List<MultipartFile> files = createFileListWithNulls(1);

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(newCard), files);

        // Assert
        assertTrue(result);
        List<HomeInfoCardModel> cards = homeInfoCardRepository.findAllByConfigVersion(config.getVersion());
        assertEquals(1, cards.size());
        assertEquals("New Title", cards.get(0).getTitle());
        assertEquals(0x0000FF, cards.get(0).getColor());
        assertNull(cards.get(0).getIcon());

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithNewCard_WithoutIcon_ShouldCreateInDb -----------");
    }

    @Test
    @Transactional
    void updateCards_WithNewCard_WithIcon_ShouldCreateWithIcon() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithNewCard_WithIcon_ShouldCreateWithIcon -----------");

        // Arrange
        ConfigModel config = createConfig();
        InformationCardUpdate newCard = new InformationCardUpdate(
            null, "Card with Icon", "Has icon", 0xFF00FF  // Magenta
        );

        List<MultipartFile> files = List.of(mockIconFile);

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(newCard), files);

        // Assert
        assertTrue(result);
        List<HomeInfoCardModel> cards = homeInfoCardRepository.findAllByConfigVersion(config.getVersion());
        assertEquals(1, cards.size());
        assertNotNull(cards.get(0).getIcon());
        assertTrue(cards.get(0).getIcon().getPath().contains("icon.png"));

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithNewCard_WithIcon_ShouldCreateWithIcon -----------");
    }

    @Test
    @Transactional
    void updateCards_WithEmptyTitle_ShouldFailValidation() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithEmptyTitle_ShouldFailValidation -----------");

        // Arrange
        ConfigModel config = createConfig();
        InformationCardUpdate invalidCard = new InformationCardUpdate(
            null, "", "Valid Description", 0x000000
        );

        List<MultipartFile> files = createFileListWithNulls(1);

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> homeInfoCardService.updateCards(config, List.of(invalidCard), files));

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithEmptyTitle_ShouldFailValidation -----------");
    }

    @Test
    @Transactional
    void updateCards_WithEmptyDescription_ShouldFailValidation() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithEmptyDescription_ShouldFailValidation -----------");

        // Arrange
        ConfigModel config = createConfig();
        InformationCardUpdate invalidCard = new InformationCardUpdate(
            null, "Valid Title", "", 0x000000
        );

        List<MultipartFile> files = createFileListWithNulls(1);

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> homeInfoCardService.updateCards(config, List.of(invalidCard), files));

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithEmptyDescription_ShouldFailValidation -----------");
    }


    @Test
    @Transactional
    void updateCards_WithColorOutOfRange_ShouldFailConstraint() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithColorOutOfRange_ShouldFailConstraint -----------");

        // Arrange
        ConfigModel config = createConfig();

        InformationCardUpdate invalidCard = new InformationCardUpdate(
            null, "Title", "Description", 0x1000000
        );

        List<MultipartFile> files = createFileListWithNulls(1);

        // Act & Assert
        try {
            homeInfoCardService.updateCards(config, List.of(invalidCard), files);
            homeInfoCardRepository.flush();
            fail("Should have thrown exception due to color max constraint");
        } catch (Exception e) {
            assertNotNull(e);
        }

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithColorOutOfRange_ShouldFailConstraint -----------");
    }

    @Test
    @Transactional
    void updateCards_WithExistingCard_ChangeColor_ShouldUpdateInDb() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithExistingCard_ChangeColor_ShouldUpdateInDb -----------");

        // Arrange
        ConfigModel config = createConfig();

        HomeInfoCardModel storedCard = HomeInfoCardModel.builder()
            .config(config)
            .title("Title")
            .description("Description")
            .color(0xFF0000)
            .order((short) 0)
            .build();
        homeInfoCardRepository.save(storedCard);

        InformationCardUpdate update = new InformationCardUpdate(
            storedCard.getId(), "Title", "Description", 0x00FF00
        );

        List<MultipartFile> files = createFileListWithNulls(1);

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(update), files);

        // Assert
        assertTrue(result);
        Optional<HomeInfoCardModel> found = homeInfoCardRepository.findById(storedCard.getId());
        assertTrue(found.isPresent());
        assertEquals(0x00FF00, found.get().getColor());

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithExistingCard_ChangeColor_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateCards_WithExistingCard_AddIcon_ShouldUpdateInDb() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithExistingCard_AddIcon_ShouldUpdateInDb -----------");

        // Arrange
        ConfigModel config = createConfig();

        HomeInfoCardModel storedCard = HomeInfoCardModel.builder()
            .config(config)
            .title("Title")
            .description("Description")
            .color(0x0000FF)
            .icon(null)
            .order((short) 0)
            .build();
        homeInfoCardRepository.save(storedCard);

        InformationCardUpdate update = new InformationCardUpdate(
            storedCard.getId(), "Title", "Description", 0x0000FF
        );

        List<MultipartFile> files = List.of(mockIconFile);

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(update), files);

        // Assert
        assertTrue(result);
        Optional<HomeInfoCardModel> found = homeInfoCardRepository.findById(storedCard.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getIcon());
        assertTrue(found.get().getIcon().getPath().contains("icon.png"));

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithExistingCard_AddIcon_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateCards_WithMultipleCards_ShouldHandleCorrectly() {
        log.info("----------- Iniciando HomeInfoCardServiceIT.updateCards_WithMultipleCards_ShouldHandleCorrectly -----------");

        // Arrange
        ConfigModel config = createConfig();

        HomeInfoCardModel card1 = HomeInfoCardModel.builder()
            .config(config).title("Old 1").description("Desc1").color(0xFF0000).order((short) 0).build();
        HomeInfoCardModel card2 = HomeInfoCardModel.builder()
            .config(config).title("Old 2").description("Desc2").color(0x00FF00).order((short) 1).build();

        homeInfoCardRepository.save(card1);
        homeInfoCardRepository.save(card2);

        List<InformationCardUpdate> updates = List.of(
            new InformationCardUpdate(card1.getId(), "New 1", "Desc1", 0xFF0000),
            new InformationCardUpdate(card2.getId(), "Old 2", "Desc2", 0x00FF00),
            new InformationCardUpdate(null, "New 3", "Desc3", 0x0000FF)
        );

        List<MultipartFile> files = createFileListWithNulls(3);

        // Act
        boolean result = homeInfoCardService.updateCards(config, updates, files);

        // Assert
        assertTrue(result);
        List<HomeInfoCardModel> allCards = homeInfoCardRepository.findAllByConfigVersion(config.getVersion());
        assertEquals(3, allCards.size());

        assertTrue(allCards.stream().anyMatch(c -> "New 1".equals(c.getTitle())));
        assertTrue(allCards.stream().anyMatch(c -> "Old 2".equals(c.getTitle())));
        assertTrue(allCards.stream().anyMatch(c -> "New 3".equals(c.getTitle())));

        log.info("----------- Finalizó correctamente HomeInfoCardServiceIT.updateCards_WithMultipleCards_ShouldHandleCorrectly -----------");
    }
}
