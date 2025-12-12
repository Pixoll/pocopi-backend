package com.pocopi.api.integration.services.test;

import com.pocopi.api.dto.test.TestOptionUpdate;
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
import com.pocopi.api.services.TestQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
class TestQuestionServiceIT {

    @Autowired
    private TestQuestionService testQuestionService;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Autowired
    private TestPhaseRepository testPhaseRepository;

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

    private TestPhaseModel createMinimalPhase() {
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
        group = testGroupRepository.save(group);

        TestPhaseModel phase = TestPhaseModel.builder()
            .group(group)
            .order((short) 0)
            .build();
        return testPhaseRepository.save(phase);
    }

    @Test
    @Transactional
    void cloneQuestions_WhenQuestionsExist_ShouldCopyQuestionsAndOptionsAndImages() {
        // Arrange
        TestPhaseModel originalPhase = createMinimalPhase();

        TestQuestionModel qWithImg = TestQuestionModel.builder()
            .phase(originalPhase)
            .order((short) 0)
            .text("Q with img")
            .build();
        qWithImg = testQuestionRepository.save(qWithImg);

        ImageModel questionImage = imageService.saveImageFile(ImageService.ImageCategory.TEST_QUESTION, mockPngFile, "q-img");
        qWithImg.setImage(questionImage);
        testQuestionRepository.save(qWithImg);

        ImageModel optionImage = imageService.saveImageFile(ImageService.ImageCategory.TEST_OPTION, mockJpgFile, "opt-img");
        TestOptionModel opt1 = TestOptionModel.builder()
            .question(qWithImg)
            .order((short) 0)
            .text("Opt A")
            .image(optionImage)
            .correct(true)
            .build();
        TestOptionModel opt2 = TestOptionModel.builder()
            .question(qWithImg)
            .order((short) 1)
            .text("Opt B")
            .correct(false)
            .build();
        testOptionRepository.save(opt1);
        testOptionRepository.save(opt2);

        TestPhaseModel newPhase = TestPhaseModel.builder()
            .group(originalPhase.getGroup())
            .order((short) 1)
            .build();
        newPhase = testPhaseRepository.save(newPhase);

        // Act
        testQuestionService.cloneQuestions(originalPhase.getId(), newPhase);

        // Assert
        List<TestQuestionModel> clonedQuestions = testQuestionRepository.findAllByPhaseId(newPhase.getId());
        assertEquals(1, clonedQuestions.size());

        TestQuestionModel clonedQ = clonedQuestions.get(0);
        assertEquals("Q with img", clonedQ.getText());
        assertNotNull(clonedQ.getImage());
        assertNotEquals(questionImage.getId(), clonedQ.getImage().getId());

        List<TestOptionModel> clonedOptions = testOptionRepository.findAllByQuestionId(clonedQ.getId());
        assertEquals(2, clonedOptions.size());

        Optional<TestOptionModel> clonedOptWithImg = clonedOptions.stream().filter(o -> "Opt A".equals(o.getText())).findFirst();
        assertTrue(clonedOptWithImg.isPresent());
        assertNotNull(clonedOptWithImg.get().getImage());
        assertNotEquals(optionImage.getId(), clonedOptWithImg.get().getImage().getId());

        assertTrue(Files.exists(resolveOnDisk(clonedQ.getImage())));
        assertTrue(Files.exists(resolveOnDisk(clonedOptWithImg.get().getImage())));
    }

    @Test
    @Transactional
    void updateQuestions_WithNewQuestion_ShouldCreateQuestionAndOptions() {
        // Arrange
        TestPhaseModel phase = createMinimalPhase();

        TestOptionUpdate optUpd = new TestOptionUpdate(null, "NewOpt", true);
        TestQuestionUpdate qUpd = new TestQuestionUpdate(null, "Created Q", false, List.of(optUpd));
        List<TestQuestionUpdate> updates = List.of(qUpd);

        Map<Integer, com.pocopi.api.models.test.TestQuestionModel> storedQuestionsMap = new HashMap<>();
        Map<Integer, com.pocopi.api.models.test.TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedQuestions = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        List<MultipartFile> imageFiles = Arrays.asList((MultipartFile) null, new MockMultipartFile("f", "empty.png", "image/png", new byte[0]));

        // Act
        boolean modified = testQuestionService.updateQuestions(
            phase,
            updates,
            storedQuestionsMap,
            storedOptionsMap,
            processedQuestions,
            processedOptions,
            imageIndex,
            imageFiles
        );

        // Assert
        assertTrue(modified);

        List<TestQuestionModel> qs = testQuestionRepository.findAllByPhaseId(phase.getId());
        assertEquals(1, qs.size());
        TestQuestionModel createdQ = qs.get(0);
        assertEquals("Created Q", createdQ.getText());

        List<TestOptionModel> options = testOptionRepository.findAllByQuestionId(createdQ.getId());
        assertEquals(1, options.size());
        assertEquals("NewOpt", options.get(0).getText());
        assertTrue(options.get(0).isCorrect());
    }

    @Test
    @Transactional
    void updateQuestions_WithExistingQuestion_RemoveImage_ShouldUnsetAndDeleteImage() {
        // Arrange
        TestPhaseModel phase = createMinimalPhase();

        TestQuestionModel stored = TestQuestionModel.builder()
            .phase(phase)
            .order((short) 0)
            .text("HasImageQ")
            .build();
        stored = testQuestionRepository.save(stored);

        ImageModel img = imageService.saveImageFile(ImageService.ImageCategory.TEST_QUESTION, mockPngFile, "q-to-delete");
        stored.setImage(img);
        testQuestionRepository.save(stored);

        Map<Integer, com.pocopi.api.models.test.TestQuestionModel> storedQuestionsMap = new HashMap<>();
        storedQuestionsMap.put(stored.getId(), stored);
        Map<Integer, com.pocopi.api.models.test.TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedQuestions = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        TestQuestionUpdate update = new TestQuestionUpdate(stored.getId(), "HasImageQ", false, List.of());
        List<TestQuestionUpdate> updates = List.of(update);

        List<MultipartFile> imageFiles = Arrays.asList(new MockMultipartFile("f", "empty.png", "image/png", new byte[0]));

        // Act
        boolean modified = testQuestionService.updateQuestions(
            stored.getPhase(),
            updates,
            storedQuestionsMap,
            storedOptionsMap,
            processedQuestions,
            processedOptions,
            imageIndex,
            imageFiles
        );

        // Assert
        assertTrue(modified);

        TestQuestionModel reloaded = testQuestionRepository.findById(stored.getId()).orElseThrow();
        assertNull(reloaded.getImage());

        assertFalse(imageRepository.findById(img.getId()).isPresent());
        assertFalse(Files.exists(resolveOnDisk(img)));
    }

    private Path resolveOnDisk(ImageModel img) {
        String base = imageConfig.getBasePath();
        String rel = img.getPath().replaceFirst("^images/?", "");
        return Path.of(base).resolve(rel);
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