package com.pocopi.api.unit.services.config;

import com.pocopi.api.dto.config.Pattern;
import com.pocopi.api.dto.config.PatternUpdate;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.PatternModel;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.PatternRepository;
import com.pocopi.api.services.PatternService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatternServiceTest {

    @Mock
    private PatternRepository patternRepository;

    @Mock
    private ConfigRepository configRepository;

    @Captor
    private ArgumentCaptor<ConfigModel> configCaptor;

    private PatternService patternService;

    @BeforeEach
    void setUp() {
        patternService = new PatternService(patternRepository, configRepository);
    }

    private void setEntityId(Object entity, int id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    // ==================== getAllPatterns Tests ====================

    @Test
    void getAllPatterns_WithExistingPatterns_ShouldReturnDtoList() {
        // Arrange
        PatternModel pattern1 = PatternModel.builder()
            .name("Email Pattern")
            .regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
            .build();
        setEntityId(pattern1, 1);

        PatternModel pattern2 = PatternModel.builder()
            .name("Username Pattern")
            .regex("^[a-zA-Z0-9_]{3,20}$")
            .build();
        setEntityId(pattern2, 2);

        when(patternRepository.findAll()).thenReturn(List.of(pattern1, pattern2));

        // Act
        List<Pattern> result = patternService.getAllPatterns();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Email Pattern", result.get(0).name());
        assertEquals(1, result.get(0).id());
        assertEquals("Username Pattern", result.get(1).name());
        verify(patternRepository, times(1)).findAll();
    }

    @Test
    void getAllPatterns_WithNoPatterns_ShouldReturnEmptyList() {
        when(patternRepository.findAll()).thenReturn(List.of());
        List<Pattern> result = patternService.getAllPatterns();
        assertTrue(result.isEmpty());
    }

    // ==================== updatePattern Tests ====================

    @Test
    void updatePattern_WithNullUpdate_AndPatternAssigned_ShouldUnassignPattern() {
        // Arrange
        PatternModel assignedPattern = PatternModel.builder()
            .name("Old Pattern")
            .regex("^.*$")
            .build();
        setEntityId(assignedPattern, 1);

        ConfigModel config = ConfigModel.builder()
            .title("Config")
            .description("Desc")
            .informedConsent("Consent")
            .usernamePattern(assignedPattern)
            .build();

        // Act
        boolean result = patternService.updatePattern(config, null);

        // Assert
        assertTrue(result);
        assertNull(config.getUsernamePattern());
        verify(configRepository, times(1)).save(configCaptor.capture());
    }

    @Test
    void updatePattern_WithNullUpdate_AndNoPatternAssigned_ShouldReturnFalse() {
        ConfigModel config = ConfigModel.builder()
            .title("Config")
            .description("Desc")
            .informedConsent("Consent")
            .usernamePattern(null)
            .build();

        boolean result = patternService.updatePattern(config, null);

        assertFalse(result);
        verify(configRepository, never()).save(any());
    }

    @Test
    void updatePattern_WithInvalidRegex_ShouldThrowMultiFieldException() {
        ConfigModel config = ConfigModel.builder().build();
        PatternUpdate invalidPattern = new PatternUpdate(
            null, "Invalid", "(?<invalid>"
        );

        MultiFieldException exception = assertThrows(
            MultiFieldException.class,
            () -> patternService.updatePattern(config, invalidPattern)
        );

        assertTrue(exception.getMessage().contains("Invalid"));
        assertEquals("usernamePattern", exception.getErrors().get(0).field());
    }

    @Test
    void updatePattern_WithNewPattern_ShouldCreateAndAssign() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .title("Config")
            .usernamePattern(null)
            .build();

        PatternUpdate newPatternDto = new PatternUpdate(
            null, "New Pattern", "^[a-z]+$"
        );

        PatternModel savedPattern = PatternModel.builder()
            .name("New Pattern")
            .regex("^[a-z]+$")
            .build();
        setEntityId(savedPattern, 5);

        when(patternRepository.save(any(PatternModel.class))).thenReturn(savedPattern);

        // Act
        boolean result = patternService.updatePattern(config, newPatternDto);

        // Assert
        assertTrue(result);
        verify(patternRepository, times(1)).save(any(PatternModel.class));
        verify(configRepository, times(1)).save(configCaptor.capture());
        assertEquals(savedPattern, configCaptor.getValue().getUsernamePattern());
    }

    @Test
    void updatePattern_WithExistingPattern_NoChanges_ShouldReturnFalse() {
        // Arrange
        PatternModel existingPattern = PatternModel.builder()
            .name("Existing")
            .regex("^[a-z]+$")
            .build();
        setEntityId(existingPattern, 3);

        ConfigModel config = ConfigModel.builder()
            .usernamePattern(existingPattern)
            .build();

        PatternUpdate samePattern = new PatternUpdate(
            3, "Existing", "^[a-z]+$"
        );

        when(patternRepository.findById(3)).thenReturn(Optional.of(existingPattern));

        // Act
        boolean result = patternService.updatePattern(config, samePattern);

        // Assert
        assertFalse(result);
        verify(patternRepository, never()).save(any());
    }

    @Test
    void updatePattern_WithExistingPattern_ChangedRegex_ShouldUpdatePattern() {
        // Arrange
        PatternModel existingPattern = PatternModel.builder()
            .name("Old Name")
            .regex("^[a-z]+$")
            .build();
        setEntityId(existingPattern, 3);

        ConfigModel config = ConfigModel.builder()
            .usernamePattern(existingPattern)
            .build();

        PatternUpdate updatedPattern = new PatternUpdate(
            3, "Old Name", "^[A-Z]+$"
        );

        when(patternRepository.findById(3)).thenReturn(Optional.of(existingPattern));

        // Act
        boolean result = patternService.updatePattern(config, updatedPattern);

        // Assert
        assertTrue(result);
        verify(patternRepository, times(1)).save(existingPattern);
        assertEquals("^[A-Z]+$", existingPattern.getRegex());
    }

    @Test
    void updatePattern_WithExistingPattern_NotCurrentlyAssigned_ShouldAssign() {
        // Arrange
        PatternModel currentlyAssigned = PatternModel.builder().build();
        setEntityId(currentlyAssigned, 2);

        PatternModel targetPattern = PatternModel.builder()
            .name("Target")
            .regex(".*")
            .build();
        setEntityId(targetPattern, 5);

        ConfigModel config = ConfigModel.builder()
            .usernamePattern(currentlyAssigned)
            .build();

        PatternUpdate reassignPattern = new PatternUpdate(
            5, "Target", ".*"
        );

        when(patternRepository.findById(5)).thenReturn(Optional.of(targetPattern));

        // Act
        boolean result = patternService.updatePattern(config, reassignPattern);

        // Assert
        assertTrue(result);
        verify(configRepository, times(1)).save(configCaptor.capture());
        assertEquals(targetPattern, configCaptor.getValue().getUsernamePattern());
    }
}
