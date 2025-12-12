package com.pocopi.api.unit.services.config;

import com.pocopi.api.dto.config.FrequentlyAskedQuestion;
import com.pocopi.api.dto.config.FrequentlyAskedQuestionUpdate;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.repositories.HomeFaqRepository;
import com.pocopi.api.services.HomeFaqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeFaqServiceTest {

    @Mock
    private HomeFaqRepository homeFaqRepository;

    @Captor
    private ArgumentCaptor<HomeFaqModel> faqCaptor;

    private HomeFaqService homeFaqService;

    @BeforeEach
    void setUp() {
        homeFaqService = new HomeFaqService(homeFaqRepository);
    }

    // ==================== getFaqsByConfigVersion Tests ====================

    @Test
    void getFaqsByConfigVersion_WithExistingFaqs_ShouldReturnDtoList() {
        // Arrange
        int configVersion = 1;
        HomeFaqModel faq1 = HomeFaqModel.builder()
            .id(1)
            .question("Question 1?")
            .answer("Answer 1")
            .order((short) 0)
            .build();

        HomeFaqModel faq2 = HomeFaqModel.builder()
            .id(2)
            .question("Question 2?")
            .answer("Answer 2")
            .order((short) 1)
            .build();

        when(homeFaqRepository.findAllByConfigVersion(configVersion))
            .thenReturn(List.of(faq1, faq2));

        // Act
        List<FrequentlyAskedQuestion> result = homeFaqService.getFaqsByConfigVersion(configVersion);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.getFirst().id());
        assertEquals("Question 1?", result.getFirst().question());
        assertEquals("Answer 1", result.getFirst().answer());
        verify(homeFaqRepository, times(1)).findAllByConfigVersion(configVersion);
    }

    @Test
    void getFaqsByConfigVersion_WithNoFaqs_ShouldReturnEmptyList() {
        // Arrange
        int configVersion = 1;
        when(homeFaqRepository.findAllByConfigVersion(configVersion))
            .thenReturn(List.of());

        // Act
        List<FrequentlyAskedQuestion> result = homeFaqService.getFaqsByConfigVersion(configVersion);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(homeFaqRepository, times(1)).findAllByConfigVersion(configVersion);
    }

    // ==================== cloneFaqs Tests ====================

    @Test
    void cloneFaqs_WithExistingFaqs_ShouldSaveClonedFaqs() {
        // Arrange
        int originalConfigVersion = 1;
        ConfigModel newConfig = ConfigModel.builder()
            .version(2)
            .title("New Config")
            .build();

        HomeFaqModel originalFaq = HomeFaqModel.builder()
            .id(1)
            .question("Original Question?")
            .answer("Original Answer")
            .order((short) 0)
            .build();

        when(homeFaqRepository.findAllByConfigVersion(originalConfigVersion))
            .thenReturn(List.of(originalFaq));

        // Act
        homeFaqService.cloneFaqs(originalConfigVersion, newConfig);

        // Assert
        verify(homeFaqRepository, times(1)).findAllByConfigVersion(originalConfigVersion);
        verify(homeFaqRepository, times(1)).save(faqCaptor.capture());

        HomeFaqModel savedFaq = faqCaptor.getValue();
        assertEquals(newConfig, savedFaq.getConfig());
        assertEquals("Original Question?", savedFaq.getQuestion());
        assertEquals("Original Answer", savedFaq.getAnswer());
        assertEquals(0, savedFaq.getOrder());
    }

    @Test
    void cloneFaqs_WithNoFaqs_ShouldNotSaveAnything() {
        // Arrange
        int originalConfigVersion = 1;
        ConfigModel newConfig = ConfigModel.builder()
            .version(2)
            .build();

        when(homeFaqRepository.findAllByConfigVersion(originalConfigVersion))
            .thenReturn(List.of());

        // Act
        homeFaqService.cloneFaqs(originalConfigVersion, newConfig);

        // Assert
        verify(homeFaqRepository, times(1)).findAllByConfigVersion(originalConfigVersion);
        verify(homeFaqRepository, never()).save(any());
    }

    // ==================== updateFaqs Tests ====================

    @Test
    void updateFaqs_WithNullUpdates_AndNoStoredFaqs_ShouldReturnFalse() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .version(1)
            .build();

        when(homeFaqRepository.findAllByConfigVersion(1))
            .thenReturn(List.of());

        // Act
        boolean result = homeFaqService.updateFaqs(config, null);

        // Assert
        assertFalse(result);
        verify(homeFaqRepository, never()).deleteAll(any());
        verify(homeFaqRepository, never()).save(any());
    }

    @Test
    void updateFaqs_WithNullUpdates_AndStoredFaqs_ShouldDeleteAllAndReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .version(1)
            .build();

        HomeFaqModel storedFaq = HomeFaqModel.builder()
            .id(1)
            .question("To Delete?")
            .answer("Will be deleted")
            .build();

        when(homeFaqRepository.findAllByConfigVersion(1))
            .thenReturn(List.of(storedFaq));

        // Act
        boolean result = homeFaqService.updateFaqs(config, null);

        // Assert
        assertTrue(result);
        verify(homeFaqRepository, times(1)).deleteAll(List.of(storedFaq));
    }

    @Test
    void updateFaqs_WithNewFaq_ShouldSaveAndReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .version(1)
            .build();

        FrequentlyAskedQuestionUpdate newFaq = new FrequentlyAskedQuestionUpdate(
            null,
            "New Question?",
            "New Answer"
        );

        when(homeFaqRepository.findAllByConfigVersion(1))
            .thenReturn(List.of());

        // Act
        boolean result = homeFaqService.updateFaqs(config, List.of(newFaq));

        // Assert
        assertTrue(result);
        verify(homeFaqRepository, times(1)).save(faqCaptor.capture());

        HomeFaqModel savedFaq = faqCaptor.getValue();
        assertEquals(config, savedFaq.getConfig());
        assertEquals("New Question?", savedFaq.getQuestion());
        assertEquals("New Answer", savedFaq.getAnswer());
        assertEquals(0, savedFaq.getOrder());
    }

    @Test
    void updateFaqs_WithExistingFaq_AndNoChanges_ShouldReturnFalse() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .version(1)
            .build();

        HomeFaqModel storedFaq = HomeFaqModel.builder()
            .id(1)
            .config(config)
            .question("Existing Question?")
            .answer("Existing Answer")
            .order((short) 0)
            .build();

        FrequentlyAskedQuestionUpdate update = new FrequentlyAskedQuestionUpdate(
            1,
            "Existing Question?",
            "Existing Answer"
        );

        when(homeFaqRepository.findAllByConfigVersion(1))
            .thenReturn(List.of(storedFaq));

        // Act
        boolean result = homeFaqService.updateFaqs(config, List.of(update));

        // Assert
        assertFalse(result);
        verify(homeFaqRepository, never()).save(any());
        verify(homeFaqRepository, never()).delete(any());
    }

    @Test
    void updateFaqs_WithExistingFaq_AndChanges_ShouldUpdateAndReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .version(1)
            .build();

        HomeFaqModel storedFaq = HomeFaqModel.builder()
            .id(1)
            .config(config)
            .question("Old Question?")
            .answer("Old Answer")
            .order((short) 0)
            .build();

        FrequentlyAskedQuestionUpdate update = new FrequentlyAskedQuestionUpdate(
            1,
            "Updated Question?",
            "Updated Answer"
        );

        when(homeFaqRepository.findAllByConfigVersion(1))
            .thenReturn(List.of(storedFaq));

        // Act
        boolean result = homeFaqService.updateFaqs(config, List.of(update));

        // Assert
        assertTrue(result);
        verify(homeFaqRepository, times(1)).save(storedFaq);
        assertEquals("Updated Question?", storedFaq.getQuestion());
        assertEquals("Updated Answer", storedFaq.getAnswer());
    }

    @Test
    void updateFaqs_WithRemovedFaq_ShouldDeleteAndReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .version(1)
            .build();

        HomeFaqModel storedFaq1 = HomeFaqModel.builder()
            .id(1)
            .question("Keep this?")
            .answer("Yes")
            .order((short) 0)
            .build();

        HomeFaqModel storedFaq2 = HomeFaqModel.builder()
            .id(2)
            .question("Delete this?")
            .answer("Yes")
            .order((short) 1)
            .build();

        FrequentlyAskedQuestionUpdate update = new FrequentlyAskedQuestionUpdate(
            1,
            "Keep this?",
            "Yes"
        );

        when(homeFaqRepository.findAllByConfigVersion(1))
            .thenReturn(List.of(storedFaq1, storedFaq2));

        // Act
        boolean result = homeFaqService.updateFaqs(config, List.of(update));

        // Assert
        assertTrue(result);
        verify(homeFaqRepository, times(1)).delete(storedFaq2);
    }

    @Test
    void updateFaqs_WithOrderChange_ShouldUpdateOrderAndReturnTrue() {
        // Arrange
        ConfigModel config = ConfigModel.builder()
            .version(1)
            .build();

        HomeFaqModel storedFaq = HomeFaqModel.builder()
            .id(1)
            .question("Question?")
            .answer("Answer")
            .order((short) 5)
            .build();

        FrequentlyAskedQuestionUpdate update = new FrequentlyAskedQuestionUpdate(
            1,
            "Question?",
            "Answer"
        );

        when(homeFaqRepository.findAllByConfigVersion(1))
            .thenReturn(List.of(storedFaq));

        // Act
        boolean result = homeFaqService.updateFaqs(config, List.of(update));

        // Assert
        assertTrue(result);
        verify(homeFaqRepository, times(1)).save(storedFaq);
        assertEquals(0, storedFaq.getOrder());
    }
}
