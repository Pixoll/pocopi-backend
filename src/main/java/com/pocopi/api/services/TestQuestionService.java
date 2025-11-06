package com.pocopi.api.services;

import com.pocopi.api.dto.test.TestQuestionUpdate;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.TestQuestionRepository;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TestQuestionService {
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionService testOptionService;
    private final ImageService imageService;

    public TestQuestionService(
        TestQuestionRepository testQuestionRepository,
        TestOptionService testOptionService,
        ImageService imageService
    ) {
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionService = testOptionService;
        this.imageService = imageService;
    }

    @Transactional
    public boolean updateQuestions(
        TestPhaseModel phase,
        List<TestQuestionUpdate> questionsUpdates,
        Map<Integer, TestQuestionModel> storedQuestionsMap,
        Map<Integer, TestOptionModel> storedOptionsMap,
        Map<Integer, Boolean> processedQuestions,
        Map<Integer, Boolean> processedOptions,
        AtomicInteger imageIndex,
        Map<Integer, MultipartFile> imageFiles
    ) {
        if (questionsUpdates == null || questionsUpdates.isEmpty()) {
            return true;
        }

        boolean modified = false;

        byte order = 0;

        for (final TestQuestionUpdate questionUpdate : questionsUpdates) {
            final MultipartFile questionImageFile = imageFiles.get(imageIndex.getAndIncrement());

            final boolean isNew = questionUpdate.id() == null
                || !storedQuestionsMap.containsKey(questionUpdate.id());

            if (isNew) {
                final ImageModel image = questionImageFile != null && !questionImageFile.isEmpty()
                    ? imageService.saveImageFile(ImageCategory.TEST_QUESTION, questionImageFile, "Test question image")
                    : null;

                final TestQuestionModel newQuestion = TestQuestionModel.builder()
                    .phase(phase)
                    .order(order++)
                    .text(questionUpdate.text())
                    .image(image)
                    .randomizeOptions(questionUpdate.randomizeOptions())
                    .build();

                final TestQuestionModel savedQuestion = testQuestionRepository.save(newQuestion);

                testOptionService.updateOptions(
                    savedQuestion,
                    questionUpdate.options(),
                    storedOptionsMap,
                    processedOptions,
                    imageIndex,
                    imageFiles
                );

                modified = true;
                continue;
            }

            final int questionId = questionUpdate.id();
            final TestQuestionModel storedQuestion = storedQuestionsMap.get(questionId);

            processedQuestions.put(questionId, true);

            final boolean updated = !Objects.equals(storedQuestion.getText(), questionUpdate.text())
                || storedQuestion.isRandomizeOptions() != questionUpdate.randomizeOptions()
                || storedQuestion.getOrder() != order++
                || questionImageFile != null;

            if (!updated) {
                final boolean modifiedOptions = testOptionService.updateOptions(
                    storedQuestion,
                    questionUpdate.options(),
                    storedOptionsMap,
                    processedOptions,
                    imageIndex,
                    imageFiles
                );

                modified = modifiedOptions || modified;
                continue;
            }

            final ImageModel storedImage = storedQuestion.getImage();

            storedQuestion.setText(questionUpdate.text());
            storedQuestion.setRandomizeOptions(questionUpdate.randomizeOptions());
            storedQuestion.setOrder(order);

            if (questionImageFile != null) {
                if (storedImage == null) {
                    final ImageModel newImage = imageService.saveImageFile(
                        ImageCategory.TEST_QUESTION,
                        questionImageFile,
                        "Test question image"
                    );
                    storedQuestion.setImage(newImage);
                } else if (!questionImageFile.isEmpty()) {
                    imageService.updateImageFile(storedQuestion.getImage(), questionImageFile);
                } else {
                    storedQuestion.setImage(null);
                }
            }

            final TestQuestionModel savedQuestion = testQuestionRepository.save(storedQuestion);

            if (storedImage != null && storedQuestion.getImage() == null) {
                imageService.deleteImageIfUnused(storedImage);
            }

            testOptionService.updateOptions(
                savedQuestion,
                questionUpdate.options(),
                storedOptionsMap,
                processedOptions,
                imageIndex,
                imageFiles
            );

            modified = true;
        }

        return modified;
    }
}
