package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.dto.TestGroup.PatchOption;
import com.pocopi.api.models.image.ImageModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.TestOptionRepository;
import com.pocopi.api.services.interfaces.ImageService;
import com.pocopi.api.services.interfaces.TestOptionService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class TestOptionServiceImp implements TestOptionService {
    private final TestOptionRepository testOptionRepository;
    private final ImageService imageService;

    public TestOptionServiceImp(TestOptionRepository testOptionRepository,  ImageService imageService) {
        this.testOptionRepository = testOptionRepository;
        this.imageService = imageService;
    }

    @Override
    public Map<String, String> processOptions(
        TestQuestionModel question,
        List<PatchOption> options,
        Map<Integer, File> images,
        TestGroupServiceImp.ImageIndexTracker imageIndexTracker
    ) {
        Map<String, String> results = new HashMap<>();
        List<TestOptionModel> allExistingOptions = testOptionRepository.findAllByQuestion(question);
        Map<Integer, Boolean> processedOptions = new HashMap<>();

        for (TestOptionModel option : allExistingOptions) {
            processedOptions.put(option.getId(), false);
        }

        int order = 0;

        for (PatchOption patchOption : options) {
            if (patchOption.id().isPresent()) {
                int optionId = patchOption.id().get();
                TestOptionModel savedOption = testOptionRepository.findById(optionId).orElse(null);

                if (savedOption == null) {
                    results.put("question_" + question.getId() + "_option_" + optionId, "Option not found");
                    order++;
                    imageIndexTracker.increment();
                    continue;
                }

                File optionImage = images.get(imageIndexTracker.getIndex());
                imageIndexTracker.increment();

                boolean textChanged = !Objects.equals(savedOption.getText(), patchOption.text());
                boolean correctChanged = savedOption.isCorrect() != patchOption.correct();
                boolean orderChanged = savedOption.getOrder() != order;

                boolean hasImageChange = optionImage != null;
                boolean deleteImage = hasImageChange && optionImage.length() == 0;
                boolean replaceImage = hasImageChange && optionImage.length() > 0;

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
                TestOptionModel newOption = new TestOptionModel();
                newOption.setText(patchOption.text());
                newOption.setCorrect(patchOption.correct());
                newOption.setOrder((byte) order);
                newOption.setQuestion(question);

                TestOptionModel savedOption = testOptionRepository.save(newOption);

                File optionImage = images.get(imageIndexTracker.getIndex());
                imageIndexTracker.increment();

                if (optionImage != null && optionImage.length() > 0) {
                    updateOrCreateOptionImage(savedOption, optionImage);
                    results.put("question_" + question.getId() + "_option_new_" + order + "_image", "Image added");
                }

                results.put("question_" + question.getId() + "_option_new_" + order, "Created with ID: " + savedOption.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedOptions.entrySet()) {
            if (!entry.getValue()) {
                TestOptionModel optionToDelete = testOptionRepository.findById(entry.getKey()).orElse(null);
                if (optionToDelete != null) {
                    deleteWithImage(optionToDelete);
                    results.put("question_" + question.getId() + "_option_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    @Override
    public void deleteWithImage(TestOptionModel option) {
        ImageModel image = option.getImage();
        deleteById(option.getId());
        if (image != null) {
            imageService.deleteImage(image.getPath());
        }
    }

    @Override
    public List<TestOptionModel> findAllByQuestion(TestQuestionModel question) {
        return testOptionRepository.findAllByQuestion(question);
    }

    @Override
    public void deleteById(int id) {
        testOptionRepository.deleteById(id);
    }

    @Override
    public void save(TestOptionModel testOptionModel) {
        testOptionRepository.save(testOptionModel);
    }

    private void deleteImageFromOption(TestOptionModel option) {
        ImageModel oldImage = option.getImage();
        option.setImage(null);
        save(option);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void updateOrCreateOptionImage(TestOptionModel option, File imageFile) {
        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            ImageModel currentImage = option.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                String altText = "Test option image: " + (option.getText() != null ? option.getText() : "option");
                UploadImageResponse response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "test/questions/options",
                    imageFile.getName(),
                    altText
                );
                String path = response.url().substring(response.url().indexOf("/images/") + 1);
                ImageModel newImage = imageService.getImageModelByPath(path);
                option.setImage(newImage);
                save(option);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing option image: " + e.getMessage(), e);
        }
    }
}
