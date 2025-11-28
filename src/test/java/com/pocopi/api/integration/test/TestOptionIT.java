package com.pocopi.api.integration.test;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.TestGroupRepository;
import com.pocopi.api.repositories.TestPhaseRepository;
import com.pocopi.api.repositories.TestQuestionRepository;
import com.pocopi.api.repositories.TestOptionRepository;
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
class TestOptionIT {

    private static final Logger log = LoggerFactory.getLogger(TestOptionIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private TestPhaseRepository testPhaseRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Test
    @Transactional
    void createAndReadOptionsForQuestion() {
        log.info("----------- Iniciando TestOptionIT.createAndReadOptionsForQuestion -----------");

        // 1) Config básica
        ConfigModel config = ConfigModel.builder()
            .title("Config integración test options")
            .subtitle("Subtítulo options")
            .description("Descripción para pruebas de test_option.")
            .informedConsent("Consentimiento informado para pruebas de test_option.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        // 2) Grupo
        TestGroupModel group = TestGroupModel.builder()
            .config(savedConfig)
            .label("GRUPO-OPCIONES")
            .probability((byte) 100)
            .greeting("Grupo para pruebas de opciones.")
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);

        // 3) Fase
        TestPhaseModel phase = TestPhaseModel.builder()
            .group(savedGroup)
            .order((short) 1)
            .randomizeQuestions(false)
            .build();
        TestPhaseModel savedPhase = testPhaseRepository.save(phase);

        // 4) Pregunta
        TestQuestionModel question = TestQuestionModel.builder()
            .phase(savedPhase)
            .order((short) 1)
            .text("¿Cuál de las siguientes opciones prefieres?")
            .image(null)
            .randomizeOptions(true)
            .build();
        TestQuestionModel savedQuestion = testQuestionRepository.save(question);

        // 5) Opciones válidas
        TestOptionModel opt1 = TestOptionModel.builder()
            .question(savedQuestion)
            .order((short) 1)
            .text("Opción A")
            .image(null)
            .correct(false)
            .build();

        TestOptionModel opt2 = TestOptionModel.builder()
            .question(savedQuestion)
            .order((short) 2)
            .text("Opción B")
            .image(null)
            .correct(true)
            .build();

        TestOptionModel opt3 = TestOptionModel.builder()
            .question(savedQuestion)
            .order((short) 3)
            .text("Opción C")
            .image(null)
            .correct(false)
            .build();

        TestOptionModel savedOpt1 = testOptionRepository.save(opt1);
        TestOptionModel savedOpt2 = testOptionRepository.save(opt2);
        TestOptionModel savedOpt3 = testOptionRepository.save(opt3);

        log.info("Opciones guardadas con ids: {}, {}, {}",
            savedOpt1.getId(), savedOpt2.getId(), savedOpt3.getId());

        assertTrue(savedOpt1.getId() > 0);
        assertTrue(savedOpt2.getId() > 0);
        assertTrue(savedOpt3.getId() > 0);

        // 6) Lectura de todas las opciones de esa pregunta
        List<TestOptionModel> all = testOptionRepository.findAll();
        List<TestOptionModel> optionsForQuestion = all.stream()
            .filter(o -> o.getQuestion().getId() == savedQuestion.getId())
            .sorted(java.util.Comparator.comparingInt(TestOptionModel::getOrder))
            .toList();

        assertEquals(3, optionsForQuestion.size(), "La pregunta debe tener exactamente 3 opciones");
        assertEquals("Opción A", optionsForQuestion.get(0).getText());
        assertEquals("Opción B", optionsForQuestion.get(1).getText());
        assertEquals("Opción C", optionsForQuestion.get(2).getText());
        assertTrue(optionsForQuestion.get(1).isCorrect(), "La opción B debe estar marcada como correcta");

        log.info("----------- Finalizó correctamente TestOptionIT.createAndReadOptionsForQuestion -----------");
    }
}
