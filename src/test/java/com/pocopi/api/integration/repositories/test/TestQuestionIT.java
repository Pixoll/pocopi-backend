package com.pocopi.api.integration.repositories.test;

import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.TestGroupRepository;
import com.pocopi.api.repositories.TestPhaseRepository;
import com.pocopi.api.repositories.TestQuestionRepository;
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
class TestQuestionIT {

    private static final Logger log = LoggerFactory.getLogger(TestQuestionIT.class);

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private TestPhaseRepository testPhaseRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Test
    @Transactional
    void createAndReadQuestionsForPhase() {
        log.info("----------- Iniciando TestQuestionIT.createAndReadQuestionsForPhase -----------");

        // 1) Config básica
        ConfigModel config = ConfigModel.builder()
            .title("Config integración test questions")
            .subtitle("Subtítulo test questions")
            .description("Descripción para pruebas de test_question.")
            .informedConsent("Consentimiento informado para pruebas de test_question.")
            .anonymous(false)
            .build();
        ConfigModel savedConfig = configRepository.save(config);

        // 2) Grupo de test asociado a la misma config
        TestGroupModel group = TestGroupModel.builder()
            .config(savedConfig)
            .label("GRUPO-A")
            .probability((byte) 100)
            .greeting("Bienvenido al grupo A de pruebas.")
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);

        // 3) Fase asociada a lo que tu modelo pida
        TestPhaseModel phase = TestPhaseModel.builder()
            .group(savedGroup)
            .order((short) 1)
            .randomizeQuestions(false)
            .build();
        TestPhaseModel savedPhase = testPhaseRepository.save(phase);

        // 4) Preguntas para la fase
        TestQuestionModel q1 = TestQuestionModel.builder()
            .phase(savedPhase)
            .order((short) 1)
            .text("Pregunta 1: ¿Conoces el tema?")
            .image(null)
            .randomizeOptions(false)
            .build();

        TestQuestionModel q2 = TestQuestionModel.builder()
            .phase(savedPhase)
            .order((short) 2)
            .text("Pregunta 2: ¿Cuánta experiencia tienes?")
            .image(null)
            .randomizeOptions(true)
            .build();

        TestQuestionModel q3 = TestQuestionModel.builder()
            .phase(savedPhase)
            .order((short) 3)
            .text("Pregunta 3: ¿Te gustaría profundizar?")
            .image(null)
            .randomizeOptions(false)
            .build();

        TestQuestionModel savedQ1 = testQuestionRepository.save(q1);
        TestQuestionModel savedQ2 = testQuestionRepository.save(q2);
        TestQuestionModel savedQ3 = testQuestionRepository.save(q3);

        assertTrue(savedQ1.getId() > 0);
        assertTrue(savedQ2.getId() > 0);
        assertTrue(savedQ3.getId() > 0);

        // 5) Lectura de todas las preguntas de esa fase
        List<TestQuestionModel> allQuestions = testQuestionRepository.findAll();
        List<TestQuestionModel> forPhase = allQuestions.stream()
            .filter(q -> q.getPhase().getId() == savedPhase.getId())
            .sorted(java.util.Comparator.comparingInt(TestQuestionModel::getOrder))
            .toList();

        assertEquals(3, forPhase.size());
        assertEquals("Pregunta 1: ¿Conoces el tema?", forPhase.get(0).getText());
        assertEquals("Pregunta 2: ¿Cuánta experiencia tienes?", forPhase.get(1).getText());
        assertEquals("Pregunta 3: ¿Te gustaría profundizar?", forPhase.get(2).getText());

        log.info("----------- Finalizó correctamente TestQuestionIT.createAndReadQuestionsForPhase -----------");
    }
}
