package com.pocopi.api.services;

import com.pocopi.api.dto.image.ImageUrl;
import com.pocopi.api.dto.test.TestOptionUpdate;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.ImageRepository;
import com.pocopi.api.repositories.TestOptionRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TestOptionService {
    private final TestOptionRepository testOptionRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public TestOptionService(TestOptionRepository testOptionRepository, ImageService imageService,
                             ImageRepository imageRepository
    ) {
        this.testOptionRepository = testOptionRepository;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    public Map<String, String> processOptions(
        TestQuestionModel question,
        List<TestOptionUpdate> options,
        Map<Integer, File> images,
        TestGroupService.ImageIndexTracker imageIndexTracker
    ) {
        final Map<String, String> results = new HashMap<>();
        final List<TestOptionModel> allExistingOptions = testOptionRepository.findAllByQuestion(question);
        final Map<Integer, Boolean> processedOptions = new HashMap<>();

        for (final TestOptionModel option : allExistingOptions) {
            processedOptions.put(option.getId(), false);
        }

        int order = 0;

        for (final TestOptionUpdate patchOption : options) {
            if (patchOption.id().isPresent()) {
                final int optionId = patchOption.id().get();
                final TestOptionModel savedOption = testOptionRepository.findById(optionId).orElse(null);

                if (savedOption == null) {
                    results.put("question_" + question.getId() + "_option_" + optionId, "Option not found");
                    order++;
                    imageIndexTracker.increment();
                    continue;
                }

                final File optionImage = images.get(imageIndexTracker.getIndex());
                imageIndexTracker.increment();

                final boolean textChanged = !Objects.equals(savedOption.getText(), patchOption.text());
                final boolean correctChanged = savedOption.isCorrect() != patchOption.correct();
                final boolean orderChanged = savedOption.getOrder() != order;

                final boolean hasImageChange = optionImage != null;
                final boolean deleteImage = hasImageChange && optionImage.length() == 0;
                final boolean replaceImage = hasImageChange && optionImage.length() > 0;

                if (textChanged || correctChanged || orderChanged || deleteImage || replaceImage) {
                    savedOption.setText(patchOption.text());
                    savedOption.setCorrect(patchOption.correct());
                    savedOption.setOrder((byte) order);

                    if (deleteImage) {
                        deleteImageFromOption(savedOption);
                        results.put("question_" + question.getId() + "_option_" + optionId + "_image", "Image deleted");
                    } else if (replaceImage) {
                        updateOrCreateOptionImage(savedOption, optionImage);
                        results.put("question_" + question.getId() + "_option_" + optionId + "_image", "Image updated");
                    }

                    testOptionRepository.save(savedOption);
                    results.put("question_" + question.getId() + "_option_" + optionId, "Updated successfully");
                } else {
                    results.put("question_" + question.getId() + "_option_" + optionId, "No changes");
                }

                processedOptions.put(optionId, true);
            } else {
                final TestOptionModel newOption = new TestOptionModel();
                newOption.setText(patchOption.text());
                newOption.setCorrect(patchOption.correct());
                newOption.setOrder((byte) order);
                newOption.setQuestion(question);

                final TestOptionModel savedOption = testOptionRepository.save(newOption);

                final File optionImage = images.get(imageIndexTracker.getIndex());
                imageIndexTracker.increment();

                if (optionImage != null && optionImage.length() > 0) {
                    updateOrCreateOptionImage(savedOption, optionImage);
                    results.put("question_" + question.getId() + "_option_new_" + order + "_image", "Image added");
                }

                results.put(
                    "question_" + question.getId() + "_option_new_" + order,
                    "Created with ID: " + savedOption.getId()
                );
            }

            order++;
        }

        for (final Map.Entry<Integer, Boolean> entry : processedOptions.entrySet()) {
            if (!entry.getValue()) {
                final TestOptionModel optionToDelete = testOptionRepository.findById(entry.getKey()).orElse(null);
                if (optionToDelete != null) {
                    deleteWithImage(optionToDelete);
                    results.put("question_" + question.getId() + "_option_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    public void deleteWithImage(TestOptionModel option) {
        final ImageModel image = option.getImage();
        deleteById(option.getId());
        if (image != null) {
            imageService.deleteImage(image.getPath());
        }
    }

    public List<TestOptionModel> findAllByQuestion(TestQuestionModel question) {
        return testOptionRepository.findAllByQuestion(question);
    }

    public void deleteById(int id) {
        testOptionRepository.deleteById(id);
    }

    public void save(TestOptionModel testOptionModel) {
        testOptionRepository.save(testOptionModel);
    }

    private void deleteImageFromOption(TestOptionModel option) {
        final ImageModel oldImage = option.getImage();
        option.setImage(null);
        save(option);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void updateOrCreateOptionImage(TestOptionModel option, File imageFile) {
        try {
            final byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            final ImageModel currentImage = option.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                final String altText = "Test option image: " + (option.getText() != null ? option.getText() : "option");
                final ImageUrl response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "test/questions/options",
                    imageFile.getName(),
                    altText
                );
                final String path = response.url().substring(response.url().indexOf("/images/") + 1);
                final ImageModel newImage = imageRepository.findByPath(path)
                    .orElseThrow(() -> HttpException.notFound("Image with path " + path + " not found"));
                option.setImage(newImage);
                save(option);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing option image: " + e.getMessage(), e);
        }
    }
}
