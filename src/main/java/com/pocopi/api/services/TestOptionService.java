package com.pocopi.api.services;

import com.pocopi.api.dto.test.TestOptionUpdate;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.TestOptionRepository;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TestOptionService {
    private final TestOptionRepository testOptionRepository;
    private final ImageService imageService;

    public TestOptionService(TestOptionRepository testOptionRepository, ImageService imageService) {
        this.testOptionRepository = testOptionRepository;
        this.imageService = imageService;
    }

    public boolean updateOptions(
        TestQuestionModel question,
        List<TestOptionUpdate> optionsUpdates,
        Map<Integer, TestOptionModel> storedOptionsMap,
        Map<Integer, Boolean> processedOptions,
        AtomicInteger imageIndex,
        List<MultipartFile> imageFiles
    ) {
        if (optionsUpdates == null || optionsUpdates.isEmpty()) {
            return true;
        }

        boolean modified = false;

        byte order = 0;

        for (final TestOptionUpdate optionUpdate : optionsUpdates) {
            final MultipartFile optionImageFile = imageFiles.get(imageIndex.getAndIncrement());

            final boolean isNew = optionUpdate.id() == null
                || !storedOptionsMap.containsKey(optionUpdate.id());

            if (isNew) {
                final ImageModel image = optionImageFile != null && !optionImageFile.isEmpty()
                    ? imageService.saveImageFile(ImageCategory.TEST_QUESTION, optionImageFile, "Test option image")
                    : null;

                final TestOptionModel newOption = TestOptionModel.builder()
                    .question(question)
                    .order(order++)
                    .text(optionUpdate.text())
                    .image(image)
                    .correct(optionUpdate.correct())
                    .build();

                testOptionRepository.save(newOption);
                modified = true;
                continue;
            }

            final int optionId = optionUpdate.id();
            final TestOptionModel storedOption = storedOptionsMap.get(optionId);

            processedOptions.put(optionId, true);

            final boolean updated = !Objects.equals(storedOption.getText(), optionUpdate.text())
                || storedOption.isCorrect() != optionUpdate.correct()
                || storedOption.getOrder() != order++
                || optionImageFile != null;

            if (!updated) {
                continue;
            }

            final ImageModel storedImage = storedOption.getImage();

            storedOption.setText(optionUpdate.text());
            storedOption.setCorrect(optionUpdate.correct());
            storedOption.setOrder(order);

            if (optionImageFile != null) {
                if (storedImage == null) {
                    final ImageModel newImage = imageService.saveImageFile(
                        ImageCategory.TEST_OPTION,
                        optionImageFile,
                        "Test option image"
                    );
                    storedOption.setImage(newImage);
                } else if (!optionImageFile.isEmpty()) {
                    imageService.updateImageFile(storedOption.getImage(), optionImageFile);
                } else {
                    storedOption.setImage(null);
                }
            }

            testOptionRepository.save(storedOption);

            if (storedImage != null && storedOption.getImage() == null) {
                imageService.deleteImageIfUnused(storedImage);
            }

            modified = true;
        }

        return modified;
    }
}
