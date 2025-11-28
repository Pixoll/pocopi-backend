package com.pocopi.api.integration.form;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.FormQuestionRepository;
import com.pocopi.api.repositories.FormQuestionSliderLabelRepository;
import com.pocopi.api.repositories.FormRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("integration")
class FormQuestionSliderLabelIT {

    private static final Logger log = LoggerFactory.getLogger(FormQuestionSliderLabelIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormQuestionSliderLabelRepository sliderLabelRepository;

    @Test
    @Transactional
    void createAndReadSliderLabelsForQuestion() {
        log.info("----------- Iniciando FormQuestionSliderLabelIT.createAndReadSliderLabelsForQuestion -----------");

        // 1) Config válida
        ConfigModel config = ConfigModel.builder()
            .title("Config integración slider labels")
            .subtitle("Subtítulo slider")
            .description("Descripción para pruebas de form_question_slider_label.")
            .informedConsent("Texto de consentimiento informado para slider.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);
        assertTrue(savedConfig.getVersion() > 0);

        // 2) Form asociado
        FormModel form = FormModel.builder()
            .config(savedConfig)
            .type(FormType.PRE)
            .title("Formulario con pregunta slider")
            .build();
        FormModel savedForm = formRepository.save(form);
        assertTrue(savedForm.getId() > 0);

        // 3) Pregunta tipo SLIDER válida:
        FormQuestionModel sliderQuestion = FormQuestionModel.builder()
            .form(savedForm)
            .order((short) 1)
            .category("intensidad")
            .text("¿Qué tan intensa fue tu experiencia? (0 a 10)")
            .image(null)
            .required(true)
            .type(FormQuestionType.SLIDER)
            .min(0)
            .max(10)
            .step(1)
            .other(null)
            .minLength(null)
            .maxLength(null)
            .placeholder(null)
            .build();
        FormQuestionModel savedSliderQuestion = formQuestionRepository.save(sliderQuestion);
        assertTrue(savedSliderQuestion.getId() > 0);

        // 4) Labels válidos para esa pregunta slider:
        FormQuestionSliderLabelModel labelMin = FormQuestionSliderLabelModel.builder()
            .formQuestion(savedSliderQuestion)
            .number((short) 0)
            .label("Nada intensa")
            .build();

        FormQuestionSliderLabelModel labelMid = FormQuestionSliderLabelModel.builder()
            .formQuestion(savedSliderQuestion)
            .number((short) 5)
            .label("Moderada")
            .build();

        FormQuestionSliderLabelModel labelMax = FormQuestionSliderLabelModel.builder()
            .formQuestion(savedSliderQuestion)
            .number((short) 10)
            .label("Muy intensa")
            .build();

        FormQuestionSliderLabelModel savedLabelMin = sliderLabelRepository.save(labelMin);
        FormQuestionSliderLabelModel savedLabelMid = sliderLabelRepository.save(labelMid);
        FormQuestionSliderLabelModel savedLabelMax = sliderLabelRepository.save(labelMax);

        log.info("Labels guardados con ids: {}, {}, {}",
            savedLabelMin.getId(), savedLabelMid.getId(), savedLabelMax.getId());

        assertTrue(savedLabelMin.getId() > 0);
        assertTrue(savedLabelMid.getId() > 0);
        assertTrue(savedLabelMax.getId() > 0);

        // 5) Lectura: todos los labels de esa pregunta slider
        List<FormQuestionSliderLabelModel> all = sliderLabelRepository.findAll();
        List<FormQuestionSliderLabelModel> labelsForQuestion = all.stream()
            .filter(l -> l.getFormQuestion().getId() == savedSliderQuestion.getId())
            .sorted((a, b) -> Integer.compare(a.getNumber(), b.getNumber()))
            .toList();

        assertEquals(3, labelsForQuestion.size(), "La pregunta slider debe tener exactamente 3 labels");
        assertEquals("Nada intensa", labelsForQuestion.get(0).getLabel());
        assertEquals("Moderada", labelsForQuestion.get(1).getLabel());
        assertEquals("Muy intensa", labelsForQuestion.get(2).getLabel());

        log.info("----------- Finalizó correctamente FormQuestionSliderLabelIT.createAndReadSliderLabelsForQuestion -----------");
    }
}
