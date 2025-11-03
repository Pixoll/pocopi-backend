package com.pocopi.api.services;

import com.pocopi.api.dto.test.TestQuestionUpdate;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.repositories.TestQuestionRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TestQuestionService {
    private final TestQuestionRepository testQuestionRepository;
    private final ImageService imageService;
    private final TestOptionService testOptionService;
    private final ImageRepository imageRepository;

    public TestQuestionService(
        TestQuestionRepository testQuestionRepository, ImageService imageService,
        TestOptionService testOptionService,
        ImageRepository imageRepository
    ) {
        this.testQuestionRepository = testQuestionRepository;
        this.imageService = imageService;
        this.testOptionService = testOptionService;
        this.imageRepository = imageRepository;
    }

    public Map<String, String> processTestQuestions(
        TestPhaseModel phase,
        List<TestQuestionUpdate> questions,
        Map<Integer, File> images,
        TestGroupService.ImageIndexTracker imageIndexTracker
    ) {
        final Map<String, String> results = new HashMap<>();
        final List<TestQuestionModel> allExistingQuestions = testQuestionRepository.findAllByPhase(phase);
        final Map<Integer, Boolean> processedQuestions = new HashMap<>();

        for (final TestQuestionModel question : allExistingQuestions) {
            processedQuestions.put(question.getId(), false);
        }

        int order = 0;

        for (final TestQuestionUpdate patchQuestion : questions) {
            if (patchQuestion.id().isPresent()) {
                final int questionId = patchQuestion.id().get();
                final TestQuestionModel savedQuestion = testQuestionRepository.findById(questionId).orElse(null);

                if (savedQuestion == null) {
                    results.put("question_" + questionId, "Question not found");
                    order++;
                    imageIndexTracker.increment();
                    continue;
                }

                final File questionImage = images.get(imageIndexTracker.getIndex());
                imageIndexTracker.increment();

                final boolean textChanged = !Objects.equals(savedQuestion.getText(), patchQuestion.text());
                final boolean orderChanged = savedQuestion.getOrder() != order;

                final boolean hasImageChange = questionImage != null;
                final boolean deleteImage = hasImageChange && questionImage.length() == 0;
                final boolean replaceImage = hasImageChange && questionImage.length() > 0;

                if (textChanged || orderChanged || deleteImage || replaceImage) {
                    savedQuestion.setText(patchQuestion.text());
                    savedQuestion.setOrder((byte) order);

                    if (deleteImage) {
                        deleteImageFromQuestion(savedQuestion);
                        results.put("question_" + questionId + "_image", "Image deleted");
                    } else if (replaceImage) {
                        updateOrCreateQuestionImage(savedQuestion, questionImage);
                        results.put("question_" + questionId + "_image", "Image updated");
                    }

                    testQuestionRepository.save(savedQuestion);
                    results.put("question_" + questionId, "Updated successfully");
                } else {
                    results.put("question_" + questionId, "No changes");
                }

                final Map<String, String> optionResults = testOptionService.processOptions(
                    savedQuestion,
                    patchQuestion.options(),
                    images,
                    imageIndexTracker
                );
                results.putAll(optionResults);

                processedQuestions.put(questionId, true);
            } else {
                final TestQuestionModel newQuestion = new TestQuestionModel();
                newQuestion.setText(patchQuestion.text());
                newQuestion.setOrder((byte) order);
                newQuestion.setPhase(phase);

                final TestQuestionModel savedQuestion = testQuestionRepository.save(newQuestion);

                final File questionImage = images.get(imageIndexTracker.getIndex());
                imageIndexTracker.increment();

                if (questionImage != null && questionImage.length() > 0) {
                    updateOrCreateQuestionImage(savedQuestion, questionImage);
                    results.put("question_new_" + order + "_image", "Image added");
                }

                final Map<String, String> optionResults = testOptionService.processOptions(
                    savedQuestion,
                    patchQuestion.options(),
                    images,
                    imageIndexTracker
                );
                results.putAll(optionResults);

                results.put("question_new_" + order, "Created with ID: " + savedQuestion.getId());
            }

            order++;
        }

        for (final Map.Entry<Integer, Boolean> entry : processedQuestions.entrySet()) {
            if (!entry.getValue()) {
                final TestQuestionModel questionToDelete = testQuestionRepository.findById(entry.getKey()).orElse(null);
                if (questionToDelete != null) {
                    deleteWithOptions(questionToDelete);
                    results.put("question_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    public void deleteWithOptions(TestQuestionModel question) {
        final ImageModel questionImage = question.getImage();

        final List<TestOptionModel> options = testOptionService.findAllByQuestion(question);
        for (final TestOptionModel option : options) {
            testOptionService.deleteWithImage(option);
        }

        testQuestionRepository.deleteById(question.getId());

        if (questionImage != null) {
            imageService.deleteImage(questionImage.getPath());
        }
    }

    public List<TestQuestionModel> findAllByPhase(TestPhaseModel phase) {
        return testQuestionRepository.findAllByPhase(phase);
    }

    private void updateOrCreateQuestionImage(TestQuestionModel question, File imageFile) {
        try {
            final byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            final ImageModel currentImage = question.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                final String altText = "Test question image: " + (question.getText() != null
                    ? question.getText()
                    : "question"
                );
                final String url = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "test/questions",
                    imageFile.getName(),
                    altText
                );
                final String path = url.substring(url.indexOf("/images/") + 1);
                final ImageModel newImage = imageRepository.findByPath(path)
                    .orElseThrow(() -> HttpException.notFound("Image with path " + path + " not found"));
                question.setImage(newImage);
                testQuestionRepository.save(question);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing question image: " + e.getMessage(), e);
        }
    }

    private void deleteImageFromQuestion(TestQuestionModel question) {
        final ImageModel oldImage = question.getImage();
        question.setImage(null);
        testQuestionRepository.save(question);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }
}
