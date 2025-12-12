package com.pocopi.api.integration.services.test;


import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.*;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.config.ImageConfig;
import com.pocopi.api.services.ImageService;
import com.pocopi.api.services.TestGroupService;
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

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class TestGroupServiceIT {

    private static final Logger log = LoggerFactory.getLogger(TestGroupServiceIT.class);

    @Autowired
    private TestGroupService testGroupService;

    @Autowired
    private TestGroupRepository testGroupRepository;

    @Autowired
    private TestPhaseRepository testPhaseRepository;

    @Autowired
    private TestQuestionRepository testQuestionRepository;

    @Autowired
    private TestOptionRepository testOptionRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageConfig imageConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @TempDir
    private Path tempDir;

    private MockMultipartFile mockPngFile;

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
    }

    private ConfigModel createConfig(String title) {
        ConfigModel cfg = ConfigModel.builder()
            .title(title)
            .description("desc")
            .informedConsent("consent")
            .build();
        return configRepository.save(cfg);
    }

    private TestGroupModel createGroupWithHierarchy(ConfigModel cfg, String groupLabel, boolean optionHasImage) {
        TestGroupModel group = TestGroupModel.builder()
            .config(cfg)
            .label(groupLabel)
            .probability((byte) 100)
            .build();
        group = testGroupRepository.save(group);

        TestPhaseModel phase = TestPhaseModel.builder()
            .group(group)
            .order((short) 0)
            .build();
        phase = testPhaseRepository.save(phase);

        TestQuestionModel question = TestQuestionModel.builder()
            .phase(phase)
            .order((short) 0)
            .text("Q for " + groupLabel)
            .build();
        question = testQuestionRepository.save(question);

        ImageModel img = null;
        if (optionHasImage) {
            img = imageService.saveImageFile(ImageService.ImageCategory.TEST_OPTION, mockPngFile, "opt-img");
        }

        TestOptionModel option = TestOptionModel.builder()
            .question(question)
            .order((short) 0)
            .text("Opt for " + groupLabel)
            .image(img)
            .correct(true)
            .build();
        testOptionRepository.save(option);

        return group;
    }

    private void createUserAndUnfinishedAttemptForGroup(TestGroupModel group) {
        String longPassword = "a".repeat(60);
        UserModel user = UserModel.builder()
            .username("ut_user_" + System.nanoTime())
            .role(com.pocopi.api.models.user.Role.USER)
            .anonymous(false)
            .name("UT User")
            .email("ut@example.com")
            .age((byte) 25)
            .password(longPassword)
            .build();
        user = userRepository.save(user);

        UserTestAttemptModel attempt = UserTestAttemptModel.builder()
            .user(user)
            .group(group)
            .start(Instant.now())
            .end(null)
            .build();
        userTestAttemptRepository.save(attempt);
    }


    @Test
    @Transactional
    void getGroupsByConfigVersion_WithPopulatedConfig_ShouldReturnCorrectStructure() {
        log.info("Inicio getGroupsByConfigVersion_WithPopulatedConfig_ShouldReturnCorrectStructure");

        ConfigModel cfg = createConfig("cfg-1");
        createGroupWithHierarchy(cfg, "G1", false);

        // Act
        var groups = testGroupService.getGroupsByConfigVersion(cfg.getVersion());

        // Assert
        assertNotNull(groups);
        assertEquals(1, groups.size(), "Debe devolver 1 grupo");
        var g = groups.get(0);
        assertEquals("G1", g.label());
        assertEquals(1, g.phases().size());
        assertEquals(1, g.phases().get(0).questions().size());
        assertEquals(1, g.phases().get(0).questions().get(0).options().size());
        assertEquals("Opt for G1", g.phases().get(0).questions().get(0).options().get(0).text());

        log.info("Fin getGroupsByConfigVersion_WithPopulatedConfig_ShouldReturnCorrectStructure");
    }

    @Test
    @Transactional
    void getAssignedGroup_WithImagesAndText_ShouldMapImagesAndReturnAssignedDto() {
        log.info("Inicio getAssignedGroup_WithImagesAndText_ShouldMapImagesAndReturnAssignedDto");

        ConfigModel cfg = createConfig("cfg-2");
        TestGroupModel group = createGroupWithHierarchy(cfg, "G2", true);

        var assigned = testGroupService.getAssignedGroup(group);

        assertNotNull(assigned);
        assertEquals(group.getLabel(), assigned.label());
        assertTrue(assigned.phases().size() >= 1);
        var phase = assigned.phases().get(0);
        assertTrue(phase.questions().size() >= 1);
        var question = phase.questions().get(0);
        assertTrue(question.options().size() >= 1);
        assertEquals("Opt for G2", question.options().get(0).text());

        boolean hasImageDto = (question.image() != null) || question.options().stream().anyMatch(o -> o.image() != null);
        assertTrue(hasImageDto, "Al menos pregunta u opción debe devolver DTO de imagen cuando existe");

        log.info("Fin getAssignedGroup_WithImagesAndText_ShouldMapImagesAndReturnAssignedDto");
    }

    @Test
    @Transactional
    void cloneGroups_WithExistingGroups_ShouldCreateCopiesInNewConfig() {
        log.info("Inicio cloneGroups_WithExistingGroups_ShouldCreateCopiesInNewConfig");

        ConfigModel original = createConfig("cfg-original");
        createGroupWithHierarchy(original, "OG1", true);
        createGroupWithHierarchy(original, "OG2", false);

        ConfigModel target = createConfig("cfg-clone-target");

        // Act
        testGroupService.cloneGroups(original.getVersion(), target);

        // Assert
        var clonedGroups = testGroupRepository.findAllByConfigVersion(target.getVersion());
        assertEquals(2, clonedGroups.size(), "Deben clonarse 2 grupos al nuevo config");

        for (var cg : clonedGroups) {
            var phases = testPhaseRepository.findAllByGroupId(cg.getId());
            assertFalse(phases.isEmpty(), "Cada grupo clonado debe contener fases");
            var qs = testQuestionRepository.findAllByPhaseId(phases.get(0).getId());
            assertFalse(qs.isEmpty(), "Cada fase clonada debe contener preguntas");
        }

        log.info("Fin cloneGroups_WithExistingGroups_ShouldCreateCopiesInNewConfig");
    }

    @Test
    @Transactional
    void updateGroups_WhenStoredGroupHasUserData_ShouldThrowConflict() {
        log.info("Inicio updateGroups_WhenStoredGroupHasUserData_ShouldThrowConflict");

        ConfigModel cfg = createConfig("cfg-delete-conflict");
        TestGroupModel group = createGroupWithHierarchy(cfg, "G-del", false);

        createUserAndUnfinishedAttemptForGroup(group);

        HttpException ex = assertThrows(HttpException.class,
            () -> testGroupService.updateGroups(cfg, null, List.of()));

        assertEquals(409, ex.getStatus().value());

        log.info("Fin updateGroups_WhenStoredGroupHasUserData_ShouldThrowConflict");
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
}