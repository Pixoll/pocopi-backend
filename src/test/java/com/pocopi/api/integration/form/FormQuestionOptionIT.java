package com.pocopi.api.integration.form;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.FormQuestionOptionRepository;
import com.pocopi.api.repositories.FormQuestionRepository;
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
class FormQuestionOptionIT {

    private static final Logger log = LoggerFactory.getLogger(FormQuestionOptionIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Autowired
    private FormQuestionOptionRepository formQuestionOptionRepository;

    @Test
    @Transactional
    void createAndReadOptionsForSelectQuestion() {
        log.info("----------- Iniciando FormQuestionOptionIT.createAndReadOptionsForSelectQuestion -----------");

        // 1) Config válida
        ConfigModel config = ConfigModel.builder()
            .title("Config integración opciones")
            .subtitle("Subtítulo opciones")
            .description("Descripción para pruebas de form_question_option.")
            .informedConsent("Texto de consentimiento informado para pruebas de opciones.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);
        assertTrue(savedConfig.getVersion() > 0);

        // 2) Form asociado
        FormModel form = FormModel.builder()
            .config(savedConfig)
            .type(FormType.PRE)
            .title("Formulario con pregunta select-one")
            .build();
        FormModel savedForm = formRepository.save(form);
        assertTrue(savedForm.getId() > 0);

        // 3) Pregunta SELECT_ONE válida (text != null, type select-one, other != null, resto nulo)
        FormQuestionModel selectQuestion = FormQuestionModel.builder()
            .form(savedForm)
            .order((short) 1)
            .category("origen")
            .text("¿Cómo supiste de este test?")
            .image(null)
            .required(true)
            .type(FormQuestionType.SELECT_ONE)
            .min(null)
            .max(null)
            .step(null)
            .other(false)
            .minLength(null)
            .maxLength(null)
            .placeholder(null)
            .build();
        FormQuestionModel savedQuestion = formQuestionRepository.save(selectQuestion);
        assertTrue(savedQuestion.getId() > 0);

        // 4) Opciones válidas para esa pregunta:
        FormQuestionOptionModel opt1 = FormQuestionOptionModel.builder()
            .formQuestion(savedQuestion)
            .order((short) 1)
            .text("Redes sociales")
            .image(null)
            .build();

        FormQuestionOptionModel opt2 = FormQuestionOptionModel.builder()
            .formQuestion(savedQuestion)
            .order((short) 2)
            .text("Recomendación de un amigo")
            .image(null)
            .build();

        FormQuestionOptionModel opt3 = FormQuestionOptionModel.builder()
            .formQuestion(savedQuestion)
            .order((short) 3)
            .text("Otro medio")
            .image(null)
            .build();

        FormQuestionOptionModel savedOpt1 = formQuestionOptionRepository.save(opt1);
        FormQuestionOptionModel savedOpt2 = formQuestionOptionRepository.save(opt2);
        FormQuestionOptionModel savedOpt3 = formQuestionOptionRepository.save(opt3);

        log.info("Opciones guardadas con ids: {}, {}, {}",
            savedOpt1.getId(), savedOpt2.getId(), savedOpt3.getId());

        assertTrue(savedOpt1.getId() > 0);
        assertTrue(savedOpt2.getId() > 0);
        assertTrue(savedOpt3.getId() > 0);

        // 5) Lectura: todas las opciones de esa pregunta
        List<FormQuestionOptionModel> allOptions = formQuestionOptionRepository.findAll();
        List<FormQuestionOptionModel> optionsForQuestion = allOptions.stream()
            .filter(o -> o.getFormQuestion().getId() == savedQuestion.getId())
            .sorted((a, b) -> Short.compare(a.getOrder(), b.getOrder()))
            .toList();

        assertEquals(3, optionsForQuestion.size(), "La pregunta debe tener exactamente 3 opciones");
        assertEquals("Redes sociales", optionsForQuestion.get(0).getText());
        assertEquals("Recomendación de un amigo", optionsForQuestion.get(1).getText());
        assertEquals("Otro medio", optionsForQuestion.get(2).getText());

        log.info("----------- Finalizó correctamente FormQuestionOptionIT.createAndReadOptionsForSelectQuestion -----------");
    }
}
