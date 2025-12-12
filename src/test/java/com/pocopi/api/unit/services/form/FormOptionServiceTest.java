package com.pocopi.api.unit.services.form;

import com.pocopi.api.dto.form.FormOptionUpdate;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionOptionModel;
import com.pocopi.api.repositories.FormQuestionOptionRepository;
import com.pocopi.api.services.FormOptionService;
import com.pocopi.api.services.ImageService;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormOptionServiceTest {

    @Mock
    private FormQuestionOptionRepository formQuestionOptionRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private MultipartFile mockFile;

    @Captor
    private ArgumentCaptor<FormQuestionOptionModel> optionCaptor;

    private FormOptionService formOptionService;

    @BeforeEach
    void setUp() {
        formOptionService = new FormOptionService(formQuestionOptionRepository, imageService);
    }

    private void setEntityId(Object entity, int id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    // ==================== cloneOptions Tests ====================

    @Test
    void cloneOptions_WithExistingOptions_ShouldCloneAll() {
        // Arrange
        int originalQuestionId = 1;
        FormQuestionModel newQuestion = new FormQuestionModel();
        setEntityId(newQuestion, 2);

        ImageModel originalImage = ImageModel.builder().path("original.png").alt("Original").build();
        setEntityId(originalImage, 1);

        ImageModel clonedImage = ImageModel.builder().path("cloned.png").alt("Original").build();
        setEntityId(clonedImage, 2);

        FormQuestionOptionModel originalOption = FormQuestionOptionModel.builder()
            .text("Option 1")
            .image(originalImage)
            .order((short) 0)
            .build();
        setEntityId(originalOption, 10);

        when(formQuestionOptionRepository.findAllByFormQuestionId(originalQuestionId))
            .thenReturn(List.of(originalOption));
        when(imageService.cloneImage(originalImage)).thenReturn(clonedImage);

        // Act
        formOptionService.cloneOptions(originalQuestionId, newQuestion);

        // Assert
        verify(formQuestionOptionRepository, times(1)).save(optionCaptor.capture());
        FormQuestionOptionModel savedOption = optionCaptor.getValue();

        assertEquals(newQuestion, savedOption.getFormQuestion());
        assertEquals("Option 1", savedOption.getText());
        assertEquals(clonedImage, savedOption.getImage());
        assertEquals(0, savedOption.getOrder());
    }

    @Test
    void cloneOptions_WithMultipleOptions_ShouldCloneAllPreservingOrder() {
        // Arrange
        int originalQuestionId = 1;
        FormQuestionModel newQuestion = new FormQuestionModel();
        setEntityId(newQuestion, 2);

        FormQuestionOptionModel option1 = FormQuestionOptionModel.builder()
            .text("Option 1")
            .order((short) 0)
            .image(null)
            .build();
        setEntityId(option1, 1);

        FormQuestionOptionModel option2 = FormQuestionOptionModel.builder()
            .text("Option 2")
            .order((short) 1)
            .image(null)
            .build();
        setEntityId(option2, 2);

        when(formQuestionOptionRepository.findAllByFormQuestionId(originalQuestionId))
            .thenReturn(List.of(option1, option2));

        // Act
        formOptionService.cloneOptions(originalQuestionId, newQuestion);

        // Assert
        verify(formQuestionOptionRepository, times(2)).save(optionCaptor.capture());
        List<FormQuestionOptionModel> savedOptions = optionCaptor.getAllValues();

        assertEquals("Option 1", savedOptions.get(0).getText());
        assertEquals(0, savedOptions.get(0).getOrder());
        assertEquals("Option 2", savedOptions.get(1).getText());
        assertEquals(1, savedOptions.get(1).getOrder());
    }

    @Test
    void cloneOptions_WithOptionWithoutImage_ShouldCloneWithoutImage() {
        // Arrange
        int originalQuestionId = 1;
        FormQuestionModel newQuestion = new FormQuestionModel();
        setEntityId(newQuestion, 2);

        FormQuestionOptionModel originalOption = FormQuestionOptionModel.builder()
            .text("Option")
            .image(null)
            .order((short) 0)
            .build();

        when(formQuestionOptionRepository.findAllByFormQuestionId(originalQuestionId))
            .thenReturn(List.of(originalOption));

        // Act
        formOptionService.cloneOptions(originalQuestionId, newQuestion);

        // Assert
        verify(imageService, never()).cloneImage(any());
        verify(formQuestionOptionRepository, times(1)).save(optionCaptor.capture());
        assertNull(optionCaptor.getValue().getImage());
    }

    // ==================== updateOptions Tests ====================

    @Test
    void updateOptions_WithNullUpdates_ShouldReturnTrue() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        // Act
        boolean result = formOptionService.updateOptions(
            question, null, storedMap, processedMap, imageIndex, List.of()
        );

        // Assert
        assertTrue(result);
        verify(formQuestionOptionRepository, never()).save(any());
    }

    @Test
    void updateOptions_WithEmptyUpdates_ShouldReturnTrue() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(), storedMap, processedMap, imageIndex, List.of()
        );

        // Assert
        assertTrue(result);
        verify(formQuestionOptionRepository, never()).save(any());
    }

    @Test
    void updateOptions_WithNewOption_WithoutImage_ShouldCreateInDb() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        FormOptionUpdate newOption = new FormOptionUpdate(null, "New Option");
        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        // ✅ Usar ArrayList en lugar de List.of()
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(newOption), storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        verify(formQuestionOptionRepository, times(1)).save(optionCaptor.capture());

        FormQuestionOptionModel savedOption = optionCaptor.getValue();
        assertEquals("New Option", savedOption.getText());
        assertNull(savedOption.getImage());
        assertEquals(0, savedOption.getOrder());
        assertEquals(question, savedOption.getFormQuestion());
    }
    @Test
    void updateOptions_WithNewOption_WithImage_ShouldSaveImageAndOption() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        FormOptionUpdate newOption = new FormOptionUpdate(null, "New Option");

        ImageModel savedImage = ImageModel.builder().path("new.png").alt("New").build();
        setEntityId(savedImage, 5);

        when(mockFile.isEmpty()).thenReturn(false);
        when(imageService.saveImageFile(
            eq(ImageCategory.FORM_OPTION), eq(mockFile), eq("Form option")
        )).thenReturn(savedImage);

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = List.of(mockFile);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(newOption), storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        verify(formQuestionOptionRepository, times(1)).save(optionCaptor.capture());
        assertEquals(savedImage, optionCaptor.getValue().getImage());
    }

    @Test
    void updateOptions_WithExistingOption_NoChanges_ShouldReturnFalse() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .text("Option")
            .order((short) 0)
            .image(null)
            .formQuestion(question)
            .build();
        setEntityId(storedOption, 10);

        FormOptionUpdate update = new FormOptionUpdate(10, "Option");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(10, storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        AtomicInteger imageIndex = new AtomicInteger(0);
        // ✅ Usar ArrayList en lugar de List.of() para permitir null
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(update), storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertFalse(result);
        verify(formQuestionOptionRepository, never()).save(any());
        assertTrue(processedMap.get(10));
    }

    @Test
    void updateOptions_WithExistingOption_ChangedText_ShouldUpdate() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .text("Old Text")
            .order((short) 0)
            .image(null)
            .formQuestion(question)
            .build();
        setEntityId(storedOption, 10);

        FormOptionUpdate update = new FormOptionUpdate(10, "New Text");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(10, storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(update), storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        verify(formQuestionOptionRepository, times(1)).save(storedOption);
        assertEquals("New Text", storedOption.getText());
    }

    @Test
    void updateOptions_WithExistingOption_ChangedOrder_ShouldUpdate() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .text("Option")
            .order((short) 0)
            .image(null)
            .formQuestion(question)
            .build();
        setEntityId(storedOption, 10);

        FormOptionUpdate update = new FormOptionUpdate(10, "Option");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(10, storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null); // First option file
        imageFiles.add(null); // Second option file

        // Act - Segundo elemento en una lista implica order = 1
        boolean result = formOptionService.updateOptions(
            question, List.of(new FormOptionUpdate(null, "First"), update),
            storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        assertEquals(1, storedOption.getOrder());
    }

    @Test
    void updateOptions_WithExistingOption_AddImage_ShouldUpdateImage() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .text("Option")
            .order((short) 0)
            .image(null)
            .formQuestion(question)
            .build();
        setEntityId(storedOption, 10);

        FormOptionUpdate update = new FormOptionUpdate(10, "Option");

        ImageModel newImage = ImageModel.builder().path("new.png").alt("New").build();
        setEntityId(newImage, 5);

        when(mockFile.isEmpty()).thenReturn(false);
        when(imageService.saveImageFile(
            eq(ImageCategory.FORM_OPTION), eq(mockFile), eq("Form option")
        )).thenReturn(newImage);

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(10, storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = List.of(mockFile);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(update), storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        verify(imageService, times(1)).saveImageFile(
            eq(ImageCategory.FORM_OPTION), eq(mockFile), eq("Form option")
        );
        verify(formQuestionOptionRepository, times(1)).save(storedOption);
        assertEquals(newImage, storedOption.getImage());
    }

    @Test
    void updateOptions_WithExistingOption_ReplaceImage_ShouldUpdateImageFile() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        ImageModel oldImage = ImageModel.builder().path("old.png").alt("Old").build();
        setEntityId(oldImage, 3);

        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .text("Option")
            .order((short) 0)
            .image(oldImage)
            .formQuestion(question)
            .build();
        setEntityId(storedOption, 10);

        FormOptionUpdate update = new FormOptionUpdate(10, "Option");

        when(mockFile.isEmpty()).thenReturn(false);

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(10, storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = List.of(mockFile);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(update), storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        verify(imageService, times(1)).updateImageFile(
            eq(ImageCategory.FORM_OPTION), eq(oldImage), eq(mockFile)
        );
        verify(formQuestionOptionRepository, times(1)).save(storedOption);
    }

    @Test
    void updateOptions_WithExistingOption_DeleteImage_ShouldRemoveImage() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        ImageModel oldImage = ImageModel.builder().path("old.png").alt("Old").build();
        setEntityId(oldImage, 3);

        FormQuestionOptionModel storedOption = FormQuestionOptionModel.builder()
            .text("Option")
            .order((short) 0)
            .image(oldImage)
            .formQuestion(question)
            .build();
        setEntityId(storedOption, 10);

        FormOptionUpdate update = new FormOptionUpdate(10, "Option");

        when(mockFile.isEmpty()).thenReturn(true); // Empty file = delete image

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(10, storedOption);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = List.of(mockFile);

        // Act
        boolean result = formOptionService.updateOptions(
            question, List.of(update), storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        assertNull(storedOption.getImage());
        verify(imageService, times(1)).deleteImageIfUnused(oldImage);
        verify(formQuestionOptionRepository, times(1)).save(storedOption);
    }

    @Test
    void updateOptions_WithMultipleOptions_MixedOperations_ShouldHandleCorrectly() {
        // Arrange
        FormQuestionModel question = new FormQuestionModel();
        setEntityId(question, 1);

        FormQuestionOptionModel existing = FormQuestionOptionModel.builder()
            .text("Existing")
            .order((short) 0)
            .formQuestion(question)
            .build();
        setEntityId(existing, 10);

        FormOptionUpdate newOption1 = new FormOptionUpdate(null, "New 1");
        FormOptionUpdate existingUpdate = new FormOptionUpdate(10, "Existing"); // No cambios
        FormOptionUpdate newOption2 = new FormOptionUpdate(null, "New 2");

        Map<Integer, FormQuestionOptionModel> storedMap = new HashMap<>();
        storedMap.put(10, existing);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);
        imageFiles.add(null);
        imageFiles.add(null);

        // Act
        boolean result = formOptionService.updateOptions(
            question,
            List.of(newOption1, existingUpdate, newOption2),
            storedMap, processedMap, imageIndex, imageFiles
        );

        // Assert
        assertTrue(result);
        verify(formQuestionOptionRepository, times(3)).save(any());
        assertTrue(processedMap.get(10));
    }
}
