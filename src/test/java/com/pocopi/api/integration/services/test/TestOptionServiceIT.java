package com.pocopi.api.integration.services.test;

import com.pocopi.api.dto.test.TestOptionUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.services.ImageService;
import com.pocopi.api.services.TestOptionService;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class TestOptionServiceIT {

    private static final Logger log = LoggerFactory.getLogger(TestOptionServiceIT.class);

    @Autowired
    private TestOptionService testOptionService;

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

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

    private TestQuestionModel createMinimalQuestion(String questionText) {
        ConfigModel cfg = ConfigModel.builder()
            .title("cfg")
            .description("d")
            .informedConsent("c")
            .build();
        ConfigModel savedCfg = configRepository.save(cfg);

        TestGroupModel group = TestGroupModel.builder()
            .config(savedCfg)
            .label("G")
            .probability((byte) 100)
            .build();
        TestGroupModel savedGroup = testGroupRepository.save(group);

        TestPhaseModel phase = TestPhaseModel.builder()
            .group(savedGroup)
            .order((short) 0)
            .build();
        TestPhaseModel savedPhase = testPhaseRepository.save(phase);

        TestQuestionModel question = TestQuestionModel.builder()
            .phase(savedPhase)
            .order((short) 0)
            .text(questionText)
            .build();
        return testQuestionRepository.save(question);
    }

    private ImageModel createImageRecord(String filename) {
        ImageModel image = ImageModel.builder()
            .path("images/test/questions/options/" + filename)
            .alt("alt " + filename)
            .build();
        return imageRepository.save(image);
    }

    private Path resolveImageOnDisk(ImageModel img) {
        String basePath = imageConfig.getBasePath();
        String relative = img.getPath();
        Path p1 = Paths.get(basePath, relative);
        if (Files.exists(p1)) {
            return p1;
        }
        Path p2 = Paths.get(basePath, relative.replaceFirst("^images/?", ""));
        if (Files.exists(p2)) {
            return p2;
        }
        return p1;
    }

    @Test
    @Transactional
    void cloneOptions_WhenOptionsExist_ShouldCopyOptionsAndImages() {
        // Arrange
        TestQuestionModel originalQuestion = createMinimalQuestion("Original Q");
        ImageModel img = imageService.saveImageFile(ImageService.ImageCategory.TEST_OPTION, mockPngFile, "opt-img");
        TestOptionModel optWithImage = TestOptionModel.builder()
            .question(originalQuestion)
            .order((short) 0)
            .text("WithImg")
            .image(img)
            .correct(false)
            .build();
        TestOptionModel optNoImage = TestOptionModel.builder()
            .question(originalQuestion)
            .order((short) 1)
            .text("NoImg")
            .correct(true)
            .build();

        testOptionRepository.save(optWithImage);
        testOptionRepository.save(optNoImage);

        TestQuestionModel newQuestion = TestQuestionModel.builder()
            .phase(originalQuestion.getPhase())
            .order((short) 1)
            .text("Cloned Q")
            .build();
        newQuestion = testQuestionRepository.save(newQuestion);

        // Act
        testOptionService.cloneOptions(originalQuestion.getId(), newQuestion);

        // Assert
        List<TestOptionModel> clonedOptions = testOptionRepository.findAllByQuestionId(newQuestion.getId());
        assertEquals(2, clonedOptions.size());

        Optional<TestOptionModel> maybeWithImage = clonedOptions.stream().filter(o -> "WithImg".equals(o.getText())).findFirst();
        assertTrue(maybeWithImage.isPresent());
        TestOptionModel clonedWithImage = maybeWithImage.get();
        assertNotNull(clonedWithImage.getImage());
        assertNotEquals(img.getId(), clonedWithImage.getImage().getId());
        assertTrue(clonedWithImage.getImage().getPath().contains("test/questions") || clonedWithImage.getImage().getPath().contains("test/questions/options"));

        Path clonedImageOnDisk = resolveImageOnDisk(clonedWithImage.getImage());
        assertTrue(Files.exists(clonedImageOnDisk));
    }

    @Test
    @Transactional
    void updateOptions_WithNullUpdates_ShouldReturnTrueAndCreateNothing() {
        // Arrange
        TestQuestionModel q = createMinimalQuestion("Q-null-updates");
        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);
        List<MultipartFile> imageFiles = List.of();

        // Act
        boolean result = testOptionService.updateOptions(q, null, storedOptionsMap, processedOptions, imageIndex, imageFiles);

        // Assert
        assertTrue(result);
        List<TestOptionModel> options = testOptionRepository.findAllByQuestionId(q.getId());
        assertTrue(options.isEmpty());
    }

    @Test
    @Transactional
    void updateOptions_WithNewOptionsAndImageFiles_ShouldCreateOptionsAndSaveImages() {
        // Arrange
        TestQuestionModel q = createMinimalQuestion("Q-create-new-options");

        List<TestOptionUpdate> updates = List.of(
            new TestOptionUpdate(null, "New 1", true),
            new TestOptionUpdate(null, "New 2", false)
        );

        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        MultipartFile file1 = mockPngFile; // use PNG bytes that Tika recognizes reliably
        MultipartFile file2 = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        List<MultipartFile> imageFiles = List.of(file1, file2);

        // Act
        boolean modified = testOptionService.updateOptions(q, updates, storedOptionsMap, processedOptions, imageIndex, imageFiles);

        // Assert
        assertTrue(modified);
        List<TestOptionModel> options = testOptionRepository.findAllByQuestionId(q.getId());
        assertEquals(2, options.size());

        Optional<TestOptionModel> withImage = options.stream().filter(o -> "New 1".equals(o.getText())).findFirst();
        assertTrue(withImage.isPresent());
        assertNotNull(withImage.get().getImage());
        ImageModel createdImage = withImage.get().getImage();
        assertTrue(imageRepository.findById(createdImage.getId()).isPresent());
        assertTrue(Files.exists(resolveImageOnDisk(createdImage)));

        Optional<TestOptionModel> withoutImage = options.stream().filter(o -> "New 2".equals(o.getText())).findFirst();
        assertTrue(withoutImage.isPresent());
        assertNull(withoutImage.get().getImage());
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_NoChange_ShouldReturnFalse() {
        // Arrange
        TestQuestionModel q = createMinimalQuestion("Q-existing-nochange");

        TestOptionModel stored = TestOptionModel.builder()
            .question(q)
            .order((short) 0)
            .text("SAME")
            .correct(true)
            .build();
        stored = testOptionRepository.save(stored);

        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        storedOptionsMap.put(stored.getId(), stored);
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        List<MultipartFile> imageFiles = new ArrayList<>();
        imageFiles.add(null);

        List<TestOptionUpdate> updates = List.of(
            new TestOptionUpdate(stored.getId(), "SAME", true)
        );

        // Act
        boolean modified = testOptionService.updateOptions(q, updates, storedOptionsMap, processedOptions, imageIndex, imageFiles);

        // Assert
        assertFalse(modified);
        Optional<TestOptionModel> reloaded = testOptionRepository.findById(stored.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("SAME", reloaded.get().getText());
        assertTrue(reloaded.get().isCorrect());
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_TextChange_ShouldModifyAndSave() {
        // Arrange
        TestQuestionModel q = createMinimalQuestion("Q-existing-change");

        TestOptionModel stored = TestOptionModel.builder()
            .question(q)
            .order((short) 0)
            .text("OLD")
            .correct(false)
            .build();
        stored = testOptionRepository.save(stored);

        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        storedOptionsMap.put(stored.getId(), stored);
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        List<MultipartFile> imageFiles = Arrays.asList((MultipartFile) null);
        List<TestOptionUpdate> updates = List.of(new TestOptionUpdate(stored.getId(), "NEW", true));

        // Act
        boolean modified = testOptionService.updateOptions(q, updates, storedOptionsMap, processedOptions, imageIndex, imageFiles);

        // Assert
        assertTrue(modified);
        TestOptionModel reloaded = testOptionRepository.findById(stored.getId()).orElseThrow();
        assertEquals("NEW", reloaded.getText());
        assertTrue(reloaded.isCorrect());
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_AddImage_ShouldSaveImage() {
        // Arrange
        TestQuestionModel q = createMinimalQuestion("Q-add-image");

        TestOptionModel stored = TestOptionModel.builder()
            .question(q)
            .order((short) 0)
            .text("NoImg")
            .correct(false)
            .image(null)
            .build();
        stored = testOptionRepository.save(stored);

        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        storedOptionsMap.put(stored.getId(), stored);
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        List<MultipartFile> imageFiles = List.of(mockPngFile);
        List<TestOptionUpdate> updates = List.of(new TestOptionUpdate(stored.getId(), "NoImg", false));

        // Act
        boolean modified = testOptionService.updateOptions(q, updates, storedOptionsMap, processedOptions, imageIndex, imageFiles);

        // Assert
        assertTrue(modified);
        TestOptionModel reloaded = testOptionRepository.findById(stored.getId()).orElseThrow();
        assertNotNull(reloaded.getImage());
        ImageModel addedImage = reloaded.getImage();
        assertTrue(imageRepository.findById(addedImage.getId()).isPresent());
        assertTrue(Files.exists(resolveImageOnDisk(addedImage)));
    }

    @Test
    @Transactional
    void updateOptions_WithExistingOption_RemoveImage_ShouldUnsetAndDeleteImage() {
        // Arrange
        TestQuestionModel q = createMinimalQuestion("Q-remove-image");

        ImageModel img = imageService.saveImageFile(ImageService.ImageCategory.TEST_OPTION, mockPngFile, "to-delete-img");
        TestOptionModel stored = TestOptionModel.builder()
            .question(q)
            .order((short) 0)
            .text("HasImg")
            .correct(false)
            .image(img)
            .build();
        stored = testOptionRepository.save(stored);

        Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();
        storedOptionsMap.put(stored.getId(), stored);
        Map<Integer, Boolean> processedOptions = new HashMap<>();
        AtomicInteger imageIndex = new AtomicInteger(0);

        MultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        List<MultipartFile> imageFiles = List.of(emptyFile);
        List<TestOptionUpdate> updates = List.of(new TestOptionUpdate(stored.getId(), "HasImg", false));

        // Act
        boolean modified = testOptionService.updateOptions(q, updates, storedOptionsMap, processedOptions, imageIndex, imageFiles);

        // Assert
        assertTrue(modified);
        TestOptionModel reloaded = testOptionRepository.findById(stored.getId()).orElseThrow();
        assertNull(reloaded.getImage());
        assertFalse(imageRepository.findById(img.getId()).isPresent());
        Path oldImgPath = resolveImageOnDisk(img);
        assertFalse(Files.exists(oldImgPath));
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
        return new byte[]{
            (byte) 0xFF, 0x00, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10
        };
    }
}