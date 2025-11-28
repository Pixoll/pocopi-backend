package com.pocopi.api.integration.form;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionType;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.repositories.ConfigRepository;
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
class FormQuestionIT {

    private static final Logger log = LoggerFactory.getLogger(FormQuestionIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormQuestionRepository formQuestionRepository;

    @Test
    @Transactional
    void createAndReadDifferentTypesOfQuestions() {
        log.info("----------- Iniciando FormQuestionIT.createAndReadDifferentTypesOfQuestions -----------");

        // Creamos una config válida
        ConfigModel config = ConfigModel.builder()
            .title("Config integración formulario")
            .subtitle("Subtítulo de prueba")
            .description("Descripción de prueba para integración de form_question.")
            .informedConsent("Texto de consentimiento informado de prueba.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        // Creamos un form a asociar
        FormModel form = FormModel.builder()
            .config(savedConfig)
            .type(FormType.PRE)
            .title("Formulario de integración PRE")
            .build();
        FormModel savedForm = formRepository.save(form);

        // Agregamos una pregunta de seleccion unica
        FormQuestionModel qSelectOne = FormQuestionModel.builder()
            .form(savedForm)
            .order((short) 1)
            .category("cat-select")
            .text("¿Cómo supiste de esta prueba?")
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

        // Agregamos uan pregunta de slider: min/max/step no null, otros campos de rango/texto null, text != null
        FormQuestionModel qSlider = FormQuestionModel.builder()
            .form(savedForm)
            .order((short) 2)
            .category("cat-slider")
            .text("Evalúa tu experiencia (1 a 5)")
            .image(null)
            .required(true)
            .type(FormQuestionType.SLIDER)
            .min(1)
            .max(5)
            .step(1)
            .other(null)
            .minLength(null)
            .maxLength(null)
            .placeholder(null)
            .build();

        // 5) Creamos una pregunta de escritura
        FormQuestionModel qTextLong = FormQuestionModel.builder()
            .form(savedForm)
            .order((short) 3)
            .category("cat-text")
            .text("Cuéntanos tu experiencia con más detalle")
            .image(null)
            .required(false)
            .type(FormQuestionType.TEXT_LONG)
            .min(null)
            .max(null)
            .step(null)
            .other(null)
            .minLength(10)
            .maxLength(500)
            .placeholder("Escribe tu respuesta aquí...")
            .build();

        FormQuestionModel savedSelectOne = formQuestionRepository.save(qSelectOne);
        FormQuestionModel savedSlider = formQuestionRepository.save(qSlider);
        FormQuestionModel savedTextLong = formQuestionRepository.save(qTextLong);

        assertTrue(savedSelectOne.getId() > 0);
        assertTrue(savedSlider.getId() > 0);
        assertTrue(savedTextLong.getId() > 0);

        List<FormQuestionModel> all = formQuestionRepository.findAll();
        long countForForm = all.stream()
            .filter(q -> q.getForm().getId() == savedForm.getId())
            .count();
        assertEquals(3, countForForm);

        log.info("----------- Finalizó correctamente FormQuestionIT.createAndReadDifferentTypesOfQuestions -----------");
    }

}
