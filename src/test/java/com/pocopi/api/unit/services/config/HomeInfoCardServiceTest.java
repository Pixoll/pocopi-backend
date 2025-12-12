package com.pocopi.api.unit.services.config;

import com.pocopi.api.dto.config.InformationCard;
import com.pocopi.api.dto.config.InformationCardUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.repositories.HomeInfoCardRepository;
import com.pocopi.api.services.HomeInfoCardService;
import com.pocopi.api.services.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeInfoCardServiceTest {

    @Mock
    private HomeInfoCardRepository homeInfoCardRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private MultipartFile mockFile;

    @Captor
    private ArgumentCaptor<HomeInfoCardModel> cardCaptor;

    private HomeInfoCardService homeInfoCardService;

    @BeforeEach
    void setUp() {
        homeInfoCardService = new HomeInfoCardService(homeInfoCardRepository, imageService);
    }

    // ==================== getCardsByConfigVersion Tests ====================

    @Test
    void getCardsByConfigVersion_WithExistingCards_ShouldReturnDtoList() {
        // Arrange
        int configVersion = 1;
        HomeInfoCardModel card1 = HomeInfoCardModel.builder()
            .id(1)
            .title("Title 1")
            .description("Desc 1")
            .color(0xFFFFFF)
            .order((short) 0)
            .build();

        when(homeInfoCardRepository.findAllByConfigVersion(configVersion))
            .thenReturn(List.of(card1));

        // Act
        List<InformationCard> result = homeInfoCardService.getCardsByConfigVersion(configVersion);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Title 1", result.getFirst().title());
        assertEquals(0xFFFFFF, result.getFirst().color());
        verify(homeInfoCardRepository, times(1)).findAllByConfigVersion(configVersion);
    }

    @Test
    void getCardsByConfigVersion_WithEmptyCards_ShouldReturnEmptyList() {
        // Arrange
        int configVersion = 1;
        when(homeInfoCardRepository.findAllByConfigVersion(configVersion))
            .thenReturn(List.of());

        // Act
        List<InformationCard> result = homeInfoCardService.getCardsByConfigVersion(configVersion);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== cloneCards Tests ====================

    @Test
    void cloneCards_WithExistingCards_ShouldCloneAll() {
        // Arrange
        int originalConfigVersion = 1;
        ConfigModel newConfig = ConfigModel.builder().version(2).build();

        ImageModel originalIcon = ImageModel.builder().id(1).build();
        ImageModel clonedIcon = ImageModel.builder().id(2).build();

        HomeInfoCardModel originalCard = HomeInfoCardModel.builder()
            .id(1)
            .title("Title")
            .description("Desc")
            .color(0x000000)
            .icon(originalIcon)
            .build();

        when(homeInfoCardRepository.findAllByConfigVersion(originalConfigVersion))
            .thenReturn(List.of(originalCard));
        when(imageService.cloneImage(originalIcon)).thenReturn(clonedIcon);

        // Act
        homeInfoCardService.cloneCards(originalConfigVersion, newConfig);

        // Assert
        verify(homeInfoCardRepository, times(1)).save(cardCaptor.capture());
        HomeInfoCardModel savedCard = cardCaptor.getValue();

        assertEquals(newConfig, savedCard.getConfig());
        assertEquals("Title", savedCard.getTitle());
        assertEquals(0x000000, savedCard.getColor());
        assertEquals(clonedIcon, savedCard.getIcon());
    }

    @Test
    void cloneCards_WithCardWithoutIcon_ShouldCloneWithoutIcon() {
        // Arrange
        int originalConfigVersion = 1;
        ConfigModel newConfig = ConfigModel.builder().version(2).build();

        HomeInfoCardModel originalCard = HomeInfoCardModel.builder()
            .id(1)
            .title("Title")
            .description("Desc")
            .color(0xFF0000)
            .icon(null)
            .build();

        when(homeInfoCardRepository.findAllByConfigVersion(originalConfigVersion))
            .thenReturn(List.of(originalCard));

        // Act
        homeInfoCardService.cloneCards(originalConfigVersion, newConfig);

        // Assert
        verify(imageService, never()).cloneImage(any());
        verify(homeInfoCardRepository, times(1)).save(cardCaptor.capture());
        assertNull(cardCaptor.getValue().getIcon());
    }

// ==================== updateCards Tests ====================

    @Test
    void updateCards_WithNullUpdatesAndExistingCards_ShouldReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        HomeInfoCardModel existingCard = HomeInfoCardModel.builder()
            .id(1)
            .title("Test")
            .build();

        when(homeInfoCardRepository.findAllByConfigVersion(1))
            .thenReturn(List.of(existingCard));

        // Act
        boolean result = homeInfoCardService.updateCards(config, null, List.of());

        // Assert
        assertTrue(result);
        verify(homeInfoCardRepository).deleteAll(anyList());
    }

    @Test
    void updateCards_WithNullUpdatesAndNoStoredCards_ShouldReturnFalse() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        when(homeInfoCardRepository.findAllByConfigVersion(1))
            .thenReturn(List.of());

        // Act
        boolean result = homeInfoCardService.updateCards(config, null, List.of());

        // Assert
        assertFalse(result);
        verify(homeInfoCardRepository, never()).save(any());
    }

    @Test
    void updateCards_WithNewCard_WithoutIcon_ShouldCreate() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        InformationCardUpdate newCard = new InformationCardUpdate(
            null, "New Title", "New Desc", 0xFFFFFF
        );

        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        List<MultipartFile> imageFiles = List.of(emptyFile);

        when(homeInfoCardRepository.findAllByConfigVersion(1)).thenReturn(List.of());

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(newCard), imageFiles);

        // Assert
        assertTrue(result);
        verify(homeInfoCardRepository, times(1)).save(cardCaptor.capture());

        HomeInfoCardModel saved = cardCaptor.getValue();
        assertEquals("New Title", saved.getTitle());
        assertEquals("New Desc", saved.getDescription());
        assertEquals(0xFFFFFF, saved.getColor());
        assertEquals(config, saved.getConfig());
        assertNull(saved.getIcon());
    }

    @Test
    void updateCards_WithNewCard_WithIcon_ShouldCreateWithIcon() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        InformationCardUpdate newCard = new InformationCardUpdate(
            null, "Title", "Desc", 0x00FF00
        );

        ImageModel savedIcon = ImageModel.builder().id(1).build();
        when(mockFile.isEmpty()).thenReturn(false);
        when(imageService.saveImageFile(
            eq(ImageService.ImageCategory.INFO_CARD), eq(mockFile), eq("Information card icon")
        )).thenReturn(savedIcon);

        List<MultipartFile> imageFiles = List.of(mockFile);

        when(homeInfoCardRepository.findAllByConfigVersion(1)).thenReturn(List.of());

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(newCard), imageFiles);

        // Assert
        assertTrue(result);
        verify(homeInfoCardRepository, times(1)).save(cardCaptor.capture());
        assertEquals(savedIcon, cardCaptor.getValue().getIcon());
    }

    @Test
    void updateCards_WithExistingCard_NoChanges_ShouldReturnFalse() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        HomeInfoCardModel storedCard = HomeInfoCardModel.builder()
            .id(1)
            .config(config)
            .title("Title")
            .description("Desc")
            .color(0xFF0000)
            .order((short) 0)
            .build();

        InformationCardUpdate update = new InformationCardUpdate(
            1, "Title", "Desc", 0xFF0000
        );

        List<MultipartFile> imageFiles = Collections.singletonList(null);  // ← Cambio aquí

        when(homeInfoCardRepository.findAllByConfigVersion(1)).thenReturn(List.of(storedCard));

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(update), imageFiles);

        // Assert
        assertFalse(result);
        verify(homeInfoCardRepository, never()).save(any());
    }

    @Test
    void updateCards_WithExistingCard_ChangeColor_ShouldUpdate() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        HomeInfoCardModel storedCard = HomeInfoCardModel.builder()
            .id(1)
            .config(config)
            .title("Title")
            .description("Desc")
            .color(0xFF0000)
            .order((short) 0)
            .build();

        InformationCardUpdate update = new InformationCardUpdate(
            1, "Title", "Desc", 0x0000FF
        );

        List<MultipartFile> imageFiles = Collections.singletonList(null);  // ← Cambio aquí

        when(homeInfoCardRepository.findAllByConfigVersion(1)).thenReturn(List.of(storedCard));

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(update), imageFiles);

        // Assert
        assertTrue(result);
        assertEquals(0x0000FF, storedCard.getColor());
        verify(homeInfoCardRepository, times(1)).save(storedCard);
    }


    @Test
    void updateCards_WithExistingCard_AddIcon_ShouldUpdateWithIcon() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        HomeInfoCardModel storedCard = HomeInfoCardModel.builder()
            .id(1)
            .config(config)
            .title("Title")
            .description("Desc")
            .color(0x00FF00)
            .icon(null)
            .order((short) 0)
            .build();

        InformationCardUpdate update = new InformationCardUpdate(
            1, "Title", "Desc", 0x00FF00
        );

        ImageModel newIcon = ImageModel.builder().id(1).build();
        when(mockFile.isEmpty()).thenReturn(false);
        when(imageService.saveImageFile(
            eq(ImageService.ImageCategory.INFO_CARD), eq(mockFile), eq("Information card icon")
        )).thenReturn(newIcon);

        List<MultipartFile> imageFiles = List.of(mockFile);

        when(homeInfoCardRepository.findAllByConfigVersion(1)).thenReturn(List.of(storedCard));

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(update), imageFiles);

        // Assert
        assertTrue(result);
        assertEquals(newIcon, storedCard.getIcon());
        verify(homeInfoCardRepository, times(1)).save(storedCard);
    }

    @Test
    void updateCards_WithExistingCard_DeleteIcon_ShouldRemoveIcon() {
        // Arrange
        ConfigModel config = ConfigModel.builder().version(1).build();
        ImageModel oldIcon = ImageModel.builder().id(1).build();

        HomeInfoCardModel storedCard = HomeInfoCardModel.builder()
            .id(1)
            .config(config)
            .title("Title")
            .description("Desc")
            .color(0xFFFFFF)
            .icon(oldIcon)
            .order((short) 0)
            .build();

        InformationCardUpdate update = new InformationCardUpdate(
            1, "Title", "Desc", 0xFFFFFF
        );

        when(mockFile.isEmpty()).thenReturn(true);

        List<MultipartFile> imageFiles = List.of(mockFile);

        when(homeInfoCardRepository.findAllByConfigVersion(1)).thenReturn(List.of(storedCard));

        // Act
        boolean result = homeInfoCardService.updateCards(config, List.of(update), imageFiles);

        // Assert
        assertTrue(result);
        assertNull(storedCard.getIcon());
        verify(imageService, times(1)).deleteImageIfUnused(oldIcon);
        verify(homeInfoCardRepository, times(1)).save(storedCard);
    }

}
