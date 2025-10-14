package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.dto.TestGroup.PatchQuestion;
import com.pocopi.api.models.image.ImageModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.TestQuestionRepository;
import com.pocopi.api.services.interfaces.ImageService;
import com.pocopi.api.services.interfaces.TestOptionService;
import com.pocopi.api.services.interfaces.TestQuestionService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class TestQuestionServiceImp implements TestQuestionService {
    private final TestQuestionRepository testQuestionRepository;
    private final ImageService imageService;
    private final TestOptionService testOptionService;

    public TestQuestionServiceImp(TestQuestionRepository testQuestionRepository,  ImageService imageService,  TestOptionService testOptionService) {
        this.testQuestionRepository = testQuestionRepository;
        this.imageService = imageService;
        this.testOptionService = testOptionService;
    }

    @Override
    public Map<String, String> processTestQuestions(
        TestPhaseModel phase,
        List<PatchQuestion> questions,
        List<Optional<File>> images,
        int imageIndex
    ) {
        Map<String, String> results = new HashMap<>();
        List<TestQuestionModel> allExistingQuestions = testQuestionRepository.findAllByPhase(phase);
        Map<Integer, Boolean> processedQuestions = new HashMap<>();

        for (TestQuestionModel question : allExistingQuestions) {
            processedQuestions.put(question.getId(), false);
        }

        int order = 0;
        int currentImageIndex = imageIndex;

        for (PatchQuestion patchQuestion : questions) {
            if (patchQuestion.id().isPresent()) {
                int questionId = patchQuestion.id().get();
                TestQuestionModel savedQuestion = testQuestionRepository.findById(questionId).orElse(null);

                if (savedQuestion == null) {
                    results.put("question_" + questionId, "Question not found");
                    order++;
                    currentImageIndex++;
                    continue;
                }

                Optional<File> questionImageOptional = (images != null && currentImageIndex < images.size())
                    ? images.get(currentImageIndex)
                    : Optional.empty();
                currentImageIndex++;

                boolean textChanged = !Objects.equals(savedQuestion.getText(), patchQuestion.text());
                boolean orderChanged = savedQuestion.getOrder() != order;

                boolean deleteImage = questionImageOptional.isPresent() && questionImageOptional.get().length() == 0;
                boolean replaceImage = questionImageOptional.isPresent() && questionImageOptional.get().length() > 0;
                boolean imageUnchanged = questionImageOptional.isEmpty();

                if (textChanged || orderChanged || deleteImage || replaceImage) {
                    savedQuestion.setText(patchQuestion.text());
                    savedQuestion.setOrder((byte) order);

                    if (deleteImage) {
                        deleteImageFromQuestion(savedQuestion);
                    } else if (replaceImage) {
                        updateOrCreateQuestionImage(savedQuestion, questionImageOptional.get());
                    }

                    testQuestionRepository.save(savedQuestion);
                    results.put("question_" + questionId, "Updated successfully");
                } else {
                    results.put("question_" + questionId, "No changes");
                }

                Map<String, String> optionResults = testOptionService.processOptions(
                    savedQuestion,
                    patchQuestion.options(),
                    images,
                    currentImageIndex
                );
                results.putAll(optionResults);

                currentImageIndex += patchQuestion.options().size();

                processedQuestions.put(questionId, true);

            } else {
                TestQuestionModel newQuestion = new TestQuestionModel();
                newQuestion.setText(patchQuestion.text());
                newQuestion.setOrder((byte) order);
                newQuestion.setPhase(phase);

                TestQuestionModel savedQuestion = testQuestionRepository.save(newQuestion);

                Optional<File> questionImageOptional = (images != null && currentImageIndex < images.size())
                    ? images.get(currentImageIndex)
                    : Optional.empty();
                currentImageIndex++;

                if (questionImageOptional.isPresent() && questionImageOptional.get().length() > 0) {
                    updateOrCreateQuestionImage(savedQuestion, questionImageOptional.get());
                }

                Map<String, String> optionResults = testOptionService.processOptions(
                    savedQuestion,
                    patchQuestion.options(),
                    images,
                    currentImageIndex
                );
                results.putAll(optionResults);

                currentImageIndex += patchQuestion.options().size();

                results.put("question_new_" + order, "Created with ID: " + savedQuestion.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedQuestions.entrySet()) {
            if (!entry.getValue()) {
                TestQuestionModel questionToDelete = testQuestionRepository.findById(entry.getKey()).orElse(null);
                if (questionToDelete != null) {
                    deleteWithOptions(questionToDelete);
                    results.put("question_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private void updateOrCreateQuestionImage(TestQuestionModel question, File imageFile) {
        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            ImageModel currentImage = question.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                String altText = "Test question image: " + (question.getText() != null ? question.getText() : "question");
                UploadImageResponse response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "test/questions",
                    imageFile.getName(),
                    altText
                );
                String path = response.url().substring(response.url().indexOf("/images/") + 1);
                ImageModel newImage = imageService.getImageModelByPath(path);
                question.setImage(newImage);
                testQuestionRepository.save(question);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing question image: " + e.getMessage(), e);
        }
    }

    private void deleteImageFromQuestion(TestQuestionModel question) {
        ImageModel oldImage = question.getImage();
        question.setImage(null);
        testQuestionRepository.save(question);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    @Override
    public void deleteWithOptions(TestQuestionModel question) {
        ImageModel questionImage = question.getImage();

        List<TestOptionModel> options = testOptionService.findAllByQuestion(question);
        for (TestOptionModel option : options) {
            testOptionService.deleteWithImage(option);
        }

        testQuestionRepository.deleteById(question.getId());

        if (questionImage != null) {
            imageService.deleteImage(questionImage.getPath());
        }
    }

    @Override
    public List<TestQuestionModel> findAllByPhase(TestPhaseModel phase) {
        return testQuestionRepository.findAllByPhase(phase);
    }
}
