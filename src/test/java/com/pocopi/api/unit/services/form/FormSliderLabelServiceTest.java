package com.pocopi.api.unit.services.form;

import com.pocopi.api.dto.form.SliderLabelUpdate;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionSliderLabelModel;
import com.pocopi.api.repositories.FormQuestionSliderLabelRepository;
import com.pocopi.api.services.FormSliderLabelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormSliderLabelServiceTest {

    @Mock
    private FormQuestionSliderLabelRepository formQuestionSliderLabelRepository;

    @Captor
    private ArgumentCaptor<FormQuestionSliderLabelModel> sliderLabelCaptor;

    private FormSliderLabelService formSliderLabelService;

    @BeforeEach
    void setUp() {
        formSliderLabelService = new FormSliderLabelService(formQuestionSliderLabelRepository);
    }

    // ==================== updateSliderLabels Tests ====================

    @Test
    void updateSliderLabels_WithNullUpdates_ShouldReturnTrue() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            null,
            storedMap,
            processedMap
        );

        // Assert
        assertTrue(result);
        verify(formQuestionSliderLabelRepository, never()).save(any());
        assertTrue(processedMap.isEmpty());
    }

    @Test
    void updateSliderLabels_WithEmptyUpdates_ShouldReturnTrue() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();
        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            List.of(),
            storedMap,
            processedMap
        );

        // Assert
        assertTrue(result);
        verify(formQuestionSliderLabelRepository, never()).save(any());
        assertTrue(processedMap.isEmpty());
    }

    @Test
    void updateSliderLabels_WithNewLabel_ShouldSaveAndReturnTrue() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        SliderLabelUpdate newLabel = new SliderLabelUpdate(
            null,
            0,
            "Strongly Disagree"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            List.of(newLabel),
            storedMap,
            processedMap
        );

        // Assert
        assertTrue(result);
        verify(formQuestionSliderLabelRepository, times(1)).save(sliderLabelCaptor.capture());

        FormQuestionSliderLabelModel savedLabel = sliderLabelCaptor.getValue();
        assertEquals(question, savedLabel.getFormQuestion());
        assertEquals(0, savedLabel.getNumber());
        assertEquals("Strongly Disagree", savedLabel.getLabel());
        assertTrue(processedMap.isEmpty()); // No se marca como procesado porque es nuevo
    }

    @Test
    void updateSliderLabels_WithNewLabelIdNotInMap_ShouldSaveAsNew() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        SliderLabelUpdate update = new SliderLabelUpdate(
            999,  // ID que no existe en el map
            5,
            "Strongly Agree"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            List.of(update),
            storedMap,
            processedMap
        );

        // Assert
        assertTrue(result);
        verify(formQuestionSliderLabelRepository, times(1)).save(any());
    }

    @Test
    void updateSliderLabels_WithExistingLabel_AndNoChanges_ShouldReturnFalse() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        FormQuestionSliderLabelModel storedLabel = FormQuestionSliderLabelModel.builder()
            .id(1)
            .formQuestion(question)
            .number(0)
            .label("Neutral")
            .build();

        SliderLabelUpdate update = new SliderLabelUpdate(
            1,
            0,
            "Neutral"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(1, storedLabel);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            List.of(update),
            storedMap,
            processedMap
        );

        // Assert
        assertFalse(result);
        verify(formQuestionSliderLabelRepository, never()).save(any());
        assertTrue(processedMap.get(1)); // Debe marcarse como procesado
    }

    @Test
    void updateSliderLabels_WithExistingLabel_AndChangedLabel_ShouldUpdateAndReturnTrue() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        FormQuestionSliderLabelModel storedLabel = FormQuestionSliderLabelModel.builder()
            .id(1)
            .formQuestion(question)
            .number(0)
            .label("Old Label")
            .build();

        SliderLabelUpdate update = new SliderLabelUpdate(
            1,
            0,
            "New Label"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(1, storedLabel);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            List.of(update),
            storedMap,
            processedMap
        );

        // Assert
        assertTrue(result);
        verify(formQuestionSliderLabelRepository, times(1)).save(storedLabel);
        assertEquals("New Label", storedLabel.getLabel());
        assertEquals(0, storedLabel.getNumber());
        assertTrue(processedMap.get(1));
    }

    @Test
    void updateSliderLabels_WithExistingLabel_AndChangedNumber_ShouldUpdateAndReturnTrue() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        FormQuestionSliderLabelModel storedLabel = FormQuestionSliderLabelModel.builder()
            .id(1)
            .formQuestion(question)
            .number(0)
            .label("Label")
            .build();

        SliderLabelUpdate update = new SliderLabelUpdate(
            1,
            5,  // Changed number
            "Label"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(1, storedLabel);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            List.of(update),
            storedMap,
            processedMap
        );

        // Assert
        assertTrue(result);
        verify(formQuestionSliderLabelRepository, times(1)).save(storedLabel);
        assertEquals("Label", storedLabel.getLabel());
        assertEquals(5, storedLabel.getNumber());
        assertTrue(processedMap.get(1));
    }

    @Test
    void updateSliderLabels_WithMultipleLabels_ShouldHandleCorrectly() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        FormQuestionSliderLabelModel storedLabel1 = FormQuestionSliderLabelModel.builder()
            .id(1)
            .formQuestion(question)
            .number(0)
            .label("Min")
            .build();

        FormQuestionSliderLabelModel storedLabel2 = FormQuestionSliderLabelModel.builder()
            .id(2)
            .formQuestion(question)
            .number(5)
            .label("Max")
            .build();

        List<SliderLabelUpdate> updates = List.of(
            new SliderLabelUpdate(1, 0, "Updated Min"),  // Update existing
            new SliderLabelUpdate(2, 5, "Max"),          // No change
            new SliderLabelUpdate(null, 3, "Middle")     // New
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(1, storedLabel1);
        storedMap.put(2, storedLabel2);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            updates,
            storedMap,
            processedMap
        );

        // Assert
        assertTrue(result);
        verify(formQuestionSliderLabelRepository, times(2)).save(any()); // 1 update + 1 new
        assertEquals("Updated Min", storedLabel1.getLabel());
        assertTrue(processedMap.get(1));
        assertTrue(processedMap.get(2));
    }

    @Test
    void updateSliderLabels_WithMultipleLabels_AndNoChanges_ShouldReturnFalse() {
        // Arrange
        FormQuestionModel question = FormQuestionModel.builder()
            .id(1)
            .build();

        FormQuestionSliderLabelModel storedLabel1 = FormQuestionSliderLabelModel.builder()
            .id(1)
            .formQuestion(question)
            .number(0)
            .label("Min")
            .build();

        FormQuestionSliderLabelModel storedLabel2 = FormQuestionSliderLabelModel.builder()
            .id(2)
            .formQuestion(question)
            .number(10)
            .label("Max")
            .build();

        List<SliderLabelUpdate> updates = List.of(
            new SliderLabelUpdate(1, 0, "Min"),
            new SliderLabelUpdate(2, 10, "Max")
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(1, storedLabel1);
        storedMap.put(2, storedLabel2);
        Map<Integer, Boolean> processedMap = new HashMap<>();

        // Act
        boolean result = formSliderLabelService.updateSliderLabels(
            question,
            updates,
            storedMap,
            processedMap
        );

        // Assert
        assertFalse(result);
        verify(formQuestionSliderLabelRepository, never()).save(any());
        assertTrue(processedMap.get(1));
        assertTrue(processedMap.get(2));
    }
}
