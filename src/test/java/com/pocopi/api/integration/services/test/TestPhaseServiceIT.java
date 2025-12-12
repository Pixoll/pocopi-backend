package com.pocopi.api.integration.services.test;

import com.pocopi.api.dto.test.TestOptionUpdate;
import com.pocopi.api.dto.test.TestPhaseUpdate;
import com.pocopi.api.dto.test.TestQuestionUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.services.ImageService;
import com.pocopi.api.services.TestPhaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class TestPhaseServiceIT {

    private static final Logger log = LoggerFactory.getLogger(TestPhaseServiceIT.class);

    @Autowired
    private TestPhaseService testPhaseService;

    @Autowired
    private TestPhaseRepository testPhaseRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageConfig imageConfig;

    @TempDir
    private Path tempDir;

    private MockMultipartFile mockPngFile;
    private MockMultipartFile mockJpgFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageConfig, "basePath", tempDir.toString());
        ReflectionTestUtils.setField(imageConfig, "baseUrl", "http://localhost:8080");

        mockPngFile = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            createPngBytes()
        );

        mockJpgFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            createJpgBytes()
        );
    }

    private TestGroupModel createMinimalGroup() {
        ConfigModel cfg = ConfigModel.builder()
            .title("cfg")
            .description("d")
            .informedConsent("c")
            .build();
        cfg = configRepository.save(cfg);

        TestGroupModel group = TestGroupModel.builder()
            .config(cfg)
            .label("G")
            .probability((byte) 100)
            .build();
        return testGroupRepository.save(group);
    }

    private TestPhaseModel createPhaseWithQuestionAndOption(TestGroupModel group, short phaseOrder, String qText, String optText) {
        TestPhaseModel phase = TestPhaseModel.builder()
            .group(group)
            .order(phaseOrder)
            .build();
        phase = testPhaseRepository.save(phase);

        TestQuestionModel q = TestQuestionModel.builder()
            .phase(phase)
            .order((short) 0)
            .text(qText)
            .build();
        q = testQuestionRepository.save(q);

        TestOptionModel opt = TestOptionModel.builder()
            .question(q)
            .order((short) 0)
            .text(optText)
            .correct(true)
            .build();
        testOptionRepository.save(opt);

        return phase;
    }

    @Test
    @Transactional
    void clonePhases_WhenPhasesExist_ShouldCopyPhasesAndDelegateQuestionsAndOptions() {
        log.info("Inicio clonePhases_WhenPhasesExist_ShouldCopyPhasesAndDelegateQuestionsAndOptions");

        // Arrange
        TestGroupModel originalGroup = createMinimalGroup();

        TestPhaseModel p1 = createPhaseWithQuestionAndOption(originalGroup, (short) 0, "Q1", "Opt1");
        TestQuestionModel q1 = testQuestionRepository.findAllByPhaseId(p1.getId()).get(0);
        ImageModel optImg = imageService.saveImageFile(ImageService.ImageCategory.TEST_OPTION, mockPngFile, "opt1-img");
        TestOptionModel optWithImg = TestOptionModel.builder()
            .question(q1)
            .order((short) 1)
            .text("Opt1-img")
            .image(optImg)
            .correct(false)
            .build();
        testOptionRepository.save(optWithImg);

        TestPhaseModel p2 = createPhaseWithQuestionAndOption(originalGroup, (short) 1, "Q2", "Opt2");

        TestGroupModel newGroup = TestGroupModel.builder()
            .config(originalGroup.getConfig())
            .label("NewG")
            .probability((byte) 0)
            .build();
        newGroup = testGroupRepository.save(newGroup);

        // Act
        testPhaseService.clonePhases(originalGroup.getId(), newGroup);

        // Assert
        List<TestPhaseModel> clonedPhases = testPhaseRepository.findAllByGroupId(newGroup.getId());
        assertEquals(2, clonedPhases.size());

        for (TestPhaseModel clonedPhase : clonedPhases) {
            List<TestQuestionModel> qs = testQuestionRepository.findAllByPhaseId(clonedPhase.getId());
            assertEquals(1, qs.size());

            List<TestOptionModel> opts = testOptionRepository.findAllByQuestionId(qs.get(0).getId());
            assertTrue(opts.size() >= 1);
        }

        Optional<TestOptionModel> clonedWithImg = testOptionRepository.findAll().stream()
            .filter(o -> "Opt1-img".equals(o.getText()) && o.getImage() != null)
            .findFirst();

        assertTrue(clonedWithImg.isPresent());
        ImageModel clonedImage = clonedWithImg.get().getImage();
        assertNotNull(clonedImage.getPath());
        assertTrue(Files.exists(Path.of(imageConfig.getBasePath()).resolve(clonedImage.getPath().replaceFirst("^images/?", ""))));

        log.info("Fin clonePhases_WhenPhasesExist_ShouldCopyPhasesAndDelegateQuestionsAndOptions");
    }

    @Test
    @Transactional
    void updatePhases_WithNullUpdates_ShouldReturnTrueAndNotModify() {
        log.info("Inicio updatePhases_WithNullUpdates_ShouldReturnTrueAndNotModify");

        // Arrange
        TestGroupModel group = createMinimalGroup();

        Map<Integer, TestPhaseModel> storedPhasesMap = new HashMap<>();
        Map<Integer, TestQuestionModel> storedQuestionsMap = new HashMap<>();
        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedPhases = new HashMap<>();
        Map<Integer, Boolean> processedQuestions = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = List.of();

        // Act
        boolean result = testPhaseService.updatePhases(
            group,
            null,
            storedPhasesMap,
            storedQuestionsMap,
            storedOptionsMap,
            processedPhases,
            processedQuestions,
            processedOptions,
            imageIndex,
            imageFiles
        );

        // Assert
        assertTrue(result);
        List<TestPhaseModel> phases = testPhaseRepository.findAllByGroupId(group.getId());
        assertTrue(phases.isEmpty());

        log.info("Fin updatePhases_WithNullUpdates_ShouldReturnTrueAndNotModify");
    }

    @Test
    @Transactional
    void updatePhases_WithNewPhase_ShouldCreatePhaseAndQuestions() {
        log.info("Inicio updatePhases_WithNewPhase_ShouldCreatePhaseAndQuestions");

        // Arrange
        TestGroupModel group = createMinimalGroup();

        TestOptionUpdate optionUpdate = new TestOptionUpdate(null, "O-New", true);
        TestQuestionUpdate questionUpdate = new TestQuestionUpdate(null, "Q-New", false, List.of(optionUpdate));
        TestPhaseUpdate phaseUpdate = new TestPhaseUpdate(null, false, List.of(questionUpdate));
        List<TestPhaseUpdate> updates = List.of(phaseUpdate);

        Map<Integer, TestPhaseModel> storedPhasesMap = new HashMap<>();
        Map<Integer, TestQuestionModel> storedQuestionsMap = new HashMap<>();
        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedPhases = new HashMap<>();
        Map<Integer, Boolean> processedQuestions = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        List<MultipartFile> imageFiles = Arrays.asList((MultipartFile) null, new MockMultipartFile("f", "empty.png", "image/png", new byte[0]));

        // Act
        boolean modified = testPhaseService.updatePhases(
            group,
            updates,
            storedPhasesMap,
            storedQuestionsMap,
            storedOptionsMap,
            processedPhases,
            processedQuestions,
            processedOptions,
            imageIndex,
            imageFiles
        );

        // Assert
        assertTrue(modified);

        List<TestPhaseModel> phases = testPhaseRepository.findAllByGroupId(group.getId());
        assertEquals(1, phases.size());

        TestPhaseModel createdPhase = phases.get(0);
        List<TestQuestionModel> qs = testQuestionRepository.findAllByPhaseId(createdPhase.getId());
        assertEquals(1, qs.size());
        List<TestOptionModel> opts = testOptionRepository.findAllByQuestionId(qs.get(0).getId());
        assertEquals(1, opts.size());

        log.info("Fin updatePhases_WithNewPhase_ShouldCreatePhaseAndQuestions");
    }

    @Test
    @Transactional
    void updatePhases_WithExistingPhase_ChangeRandomize_ShouldModifyAndSave() {
        log.info("Inicio updatePhases_WithExistingPhase_ChangeRandomize_ShouldModifyAndSave");

        // Arrange
        TestGroupModel group = createMinimalGroup();

        TestPhaseModel stored = TestPhaseModel.builder()
            .group(group)
            .order((short) 0)
            .randomizeQuestions(false)
            .build();
        stored = testPhaseRepository.save(stored);

        Map<Integer, TestPhaseModel> storedPhasesMap = new HashMap<>();
        storedPhasesMap.put(stored.getId(), stored);
        Map<Integer, TestQuestionModel> storedQuestionsMap = new HashMap<>();
        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedPhases = new HashMap<>();
        Map<Integer, Boolean> processedQuestions = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = List.of();

        TestPhaseUpdate update = new TestPhaseUpdate(stored.getId(), true, List.of());
        List<TestPhaseUpdate> updates = List.of(update);

        // Act
        boolean modified = testPhaseService.updatePhases(
            group,
            updates,
            storedPhasesMap,
            storedQuestionsMap,
            storedOptionsMap,
            processedPhases,
            processedQuestions,
            processedOptions,
            imageIndex,
            imageFiles
        );

        // Assert
        assertTrue(modified);

        TestPhaseModel reloaded = testPhaseRepository.findById(stored.getId()).orElseThrow();
        assertTrue(reloaded.isRandomizeQuestions());

        log.info("Fin updatePhases_WithExistingPhase_ChangeRandomize_ShouldModifyAndSave");
    }

    @Test
    @Transactional
    void updatePhases_WithOrderChange_ShouldUpdateOrders() {
        log.info("Inicio updatePhases_WithOrderChange_ShouldUpdateOrders");

        // Arrange
        TestGroupModel group = createMinimalGroup();

        TestPhaseModel p1 = TestPhaseModel.builder().group(group).order((short) 0).randomizeQuestions(false).build();
        TestPhaseModel p2 = TestPhaseModel.builder().group(group).order((short) 1).randomizeQuestions(false).build();
        p1 = testPhaseRepository.save(p1);
        p2 = testPhaseRepository.save(p2);

        Map<Integer, TestPhaseModel> storedPhasesMap = new HashMap<>();
        storedPhasesMap.put(p1.getId(), p1);
        storedPhasesMap.put(p2.getId(), p2);
        Map<Integer, TestQuestionModel> storedQuestionsMap = new HashMap<>();
        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedPhases = new HashMap<>();
        Map<Integer, Boolean> processedQuestions = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        TestPhaseUpdate up1 = new TestPhaseUpdate(p2.getId(), false, List.of());
        TestPhaseUpdate up2 = new TestPhaseUpdate(p1.getId(), false, List.of());
        List<TestPhaseUpdate> updates = List.of(up1, up2);

        List<MultipartFile> imageFiles = List.of();

        // Act
        boolean modified = testPhaseService.updatePhases(
            group,
            updates,
            storedPhasesMap,
            storedQuestionsMap,
            storedOptionsMap,
            processedPhases,
            processedQuestions,
            processedOptions,
            imageIndex,
            imageFiles
        );

        // Assert
        assertTrue(modified);

        TestPhaseModel reloadedP1 = testPhaseRepository.findById(p1.getId()).orElseThrow();
        TestPhaseModel reloadedP2 = testPhaseRepository.findById(p2.getId()).orElseThrow();

        assertEquals(1, reloadedP1.getOrder());
        assertEquals(0, reloadedP2.getOrder());

        log.info("Fin updatePhases_WithOrderChange_ShouldUpdateOrders");
    }

    private byte[] createPngBytes() {
        return new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4
        };
    }

    private byte[] createJpgBytes() {
        return new byte[]{ (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, (byte)0xFF, (byte)0xD9 };
    }
}