package com.pocopi.api.integration.services.form;

import com.pocopi.api.dto.form.SliderLabelUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionSliderLabelModel;
import com.pocopi.api.models.form.FormQuestionType;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.FormQuestionRepository;
import com.pocopi.api.repositories.FormQuestionSliderLabelRepository;
import com.pocopi.api.repositories.FormRepository;
import com.pocopi.api.services.FormSliderLabelService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class FormSliderLabelServiceIT {

    private static final Logger log = LoggerFactory.getLogger(FormSliderLabelServiceIT.class);

    @Autowired
    private FormSliderLabelService formSliderLabelService;

    @Autowired
    private FormQuestionSliderLabelRepository formQuestionSliderLabelRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private ConfigRepository configRepository;

    private FormQuestionModel createTestQuestion() {
        ConfigModel config = ConfigModel.builder()
            .title("Test Config")
            .description("Test Description")
            .informedConsent("Test Consent")
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        FormModel form = FormModel.builder()
            .config(savedConfig)
            .type(FormType.PRE)
            .title("Test Form")
            .build();
        FormModel savedForm = formRepository.save(form);

        FormQuestionModel question = FormQuestionModel.builder()
            .form(savedForm)
            .order((short) 0)
            .category("Test Category")
            .type(FormQuestionType.SLIDER)
            .text("Test slider question")  // ← AGREGAR ESTO
            .min(0)
            .max(10)
            .step(1)
            .build();

        return formQuestionRepository.save(question);
    }


    // ==================== updateSliderLabels Tests ====================

    @Test
    @Transactional
    void updateSliderLabels_WithNullUpdates_ShouldReturnTrueAndNotModify() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithNullUpdates_ShouldReturnTrueAndNotModify -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();
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
        List<FormQuestionSliderLabelModel> labels = formQuestionSliderLabelRepository
            .findAllByFormQuestionFormId(question.getForm().getId());
        assertTrue(labels.isEmpty());

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithNullUpdates_ShouldReturnTrueAndNotModify -----------");
    }

    @Test
    @Transactional
    void updateSliderLabels_WithEmptyUpdates_ShouldReturnTrueAndNotModify() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithEmptyUpdates_ShouldReturnTrueAndNotModify -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();
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
        List<FormQuestionSliderLabelModel> labels = formQuestionSliderLabelRepository
            .findAllByFormQuestionFormId(question.getForm().getId());
        assertTrue(labels.isEmpty());

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithEmptyUpdates_ShouldReturnTrueAndNotModify -----------");
    }

    @Test
    @Transactional
    void updateSliderLabels_WithNewLabel_ShouldCreateInDb() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithNewLabel_ShouldCreateInDb -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();

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
        List<FormQuestionSliderLabelModel> labels = formQuestionSliderLabelRepository
            .findAllByFormQuestionFormId(question.getForm().getId());
        assertEquals(1, labels.size());
        assertEquals(0, labels.getFirst().getNumber());
        assertEquals("Strongly Disagree", labels.getFirst().getLabel());
        assertEquals(question.getId(), labels.getFirst().getFormQuestion().getId());

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithNewLabel_ShouldCreateInDb -----------");
    }

    @Test
    @Transactional
    void updateSliderLabels_WithExistingLabel_AndNoChanges_ShouldReturnFalseAndNotModify() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithExistingLabel_AndNoChanges_ShouldReturnFalseAndNotModify -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();

        FormQuestionSliderLabelModel existingLabel = FormQuestionSliderLabelModel.builder()
            .formQuestion(question)
            .number(0)
            .label("Neutral")
            .build();
        FormQuestionSliderLabelModel savedLabel = formQuestionSliderLabelRepository.save(existingLabel);

        SliderLabelUpdate update = new SliderLabelUpdate(
            savedLabel.getId(),
            0,
            "Neutral"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(savedLabel.getId(), savedLabel);
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
        Optional<FormQuestionSliderLabelModel> found = formQuestionSliderLabelRepository.findById(savedLabel.getId());
        assertTrue(found.isPresent());
        assertEquals("Neutral", found.get().getLabel());
        assertEquals(0, found.get().getNumber());
        assertTrue(processedMap.get(savedLabel.getId()));

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithExistingLabel_AndNoChanges_ShouldReturnFalseAndNotModify -----------");
    }

    @Test
    @Transactional
    void updateSliderLabels_WithExistingLabel_AndChangedLabel_ShouldUpdateInDb() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithExistingLabel_AndChangedLabel_ShouldUpdateInDb -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();

        FormQuestionSliderLabelModel existingLabel = FormQuestionSliderLabelModel.builder()
            .formQuestion(question)
            .number(0)
            .label("Old Label")
            .build();
        FormQuestionSliderLabelModel savedLabel = formQuestionSliderLabelRepository.save(existingLabel);

        SliderLabelUpdate update = new SliderLabelUpdate(
            savedLabel.getId(),
            0,
            "New Label"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(savedLabel.getId(), savedLabel);
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
        Optional<FormQuestionSliderLabelModel> found = formQuestionSliderLabelRepository.findById(savedLabel.getId());
        assertTrue(found.isPresent());
        assertEquals("New Label", found.get().getLabel());
        assertEquals(0, found.get().getNumber());
        assertTrue(processedMap.get(savedLabel.getId()));

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithExistingLabel_AndChangedLabel_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateSliderLabels_WithExistingLabel_AndChangedNumber_ShouldUpdateInDb() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithExistingLabel_AndChangedNumber_ShouldUpdateInDb -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();

        FormQuestionSliderLabelModel existingLabel = FormQuestionSliderLabelModel.builder()
            .formQuestion(question)
            .number(0)
            .label("Label")
            .build();
        FormQuestionSliderLabelModel savedLabel = formQuestionSliderLabelRepository.save(existingLabel);

        SliderLabelUpdate update = new SliderLabelUpdate(
            savedLabel.getId(),
            10,  // Changed number
            "Label"
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(savedLabel.getId(), savedLabel);
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
        Optional<FormQuestionSliderLabelModel> found = formQuestionSliderLabelRepository.findById(savedLabel.getId());
        assertTrue(found.isPresent());
        assertEquals("Label", found.get().getLabel());
        assertEquals(10, found.get().getNumber());
        assertTrue(processedMap.get(savedLabel.getId()));

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithExistingLabel_AndChangedNumber_ShouldUpdateInDb -----------");
    }

    @Test
    @Transactional
    void updateSliderLabels_WithMultipleLabels_ShouldHandleCorrectly() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithMultipleLabels_ShouldHandleCorrectly -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();

        FormQuestionSliderLabelModel label1 = FormQuestionSliderLabelModel.builder()
            .formQuestion(question)
            .number(0)
            .label("Min")
            .build();
        FormQuestionSliderLabelModel savedLabel1 = formQuestionSliderLabelRepository.save(label1);

        FormQuestionSliderLabelModel label2 = FormQuestionSliderLabelModel.builder()
            .formQuestion(question)
            .number(10)
            .label("Max")
            .build();
        FormQuestionSliderLabelModel savedLabel2 = formQuestionSliderLabelRepository.save(label2);

        List<SliderLabelUpdate> updates = List.of(
            new SliderLabelUpdate(savedLabel1.getId(), 0, "Updated Min"),  // Update
            new SliderLabelUpdate(savedLabel2.getId(), 10, "Max"),         // No change
            new SliderLabelUpdate(null, 5, "Middle")                       // New
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(savedLabel1.getId(), savedLabel1);
        storedMap.put(savedLabel2.getId(), savedLabel2);
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
        List<FormQuestionSliderLabelModel> allLabels = formQuestionSliderLabelRepository
            .findAllByFormQuestionFormId(question.getForm().getId());
        assertEquals(3, allLabels.size());

        // Verificar label actualizado
        Optional<FormQuestionSliderLabelModel> updated = formQuestionSliderLabelRepository.findById(savedLabel1.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Min", updated.get().getLabel());

        // Verificar label sin cambios
        Optional<FormQuestionSliderLabelModel> unchanged = formQuestionSliderLabelRepository.findById(savedLabel2.getId());
        assertTrue(unchanged.isPresent());
        assertEquals("Max", unchanged.get().getLabel());

        // Verificar nuevo label
        FormQuestionSliderLabelModel newLabel = allLabels.stream()
            .filter(l -> "Middle".equals(l.getLabel()))
            .findFirst()
            .orElse(null);
        assertNotNull(newLabel);
        assertEquals(5, newLabel.getNumber());

        // Verificar procesados
        assertTrue(processedMap.get(savedLabel1.getId()));
        assertTrue(processedMap.get(savedLabel2.getId()));

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithMultipleLabels_ShouldHandleCorrectly -----------");
    }

    @Test
    @Transactional
    void updateSliderLabels_WithBothNumberAndLabelChange_ShouldUpdateBoth() {
        log.info("----------- Iniciando FormSliderLabelServiceIT.updateSliderLabels_WithBothNumberAndLabelChange_ShouldUpdateBoth -----------");

        // Arrange
        FormQuestionModel question = createTestQuestion();

        FormQuestionSliderLabelModel existingLabel = FormQuestionSliderLabelModel.builder()
            .formQuestion(question)
            .number(0)
            .label("Old Label")
            .build();
        FormQuestionSliderLabelModel savedLabel = formQuestionSliderLabelRepository.save(existingLabel);

        SliderLabelUpdate update = new SliderLabelUpdate(
            savedLabel.getId(),
            5,  // Changed number
            "New Label"  // Changed label
        );

        Map<Integer, FormQuestionSliderLabelModel> storedMap = new HashMap<>();
        storedMap.put(savedLabel.getId(), savedLabel);
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
        Optional<FormQuestionSliderLabelModel> found = formQuestionSliderLabelRepository.findById(savedLabel.getId());
        assertTrue(found.isPresent());
        assertEquals("New Label", found.get().getLabel());
        assertEquals(5, found.get().getNumber());
        assertTrue(processedMap.get(savedLabel.getId()));

        log.info("----------- Finalizó correctamente FormSliderLabelServiceIT.updateSliderLabels_WithBothNumberAndLabelChange_ShouldUpdateBoth -----------");
    }
}
