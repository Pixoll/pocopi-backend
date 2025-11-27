package com.pocopi.api.services;

import com.pocopi.api.dto.form.FormOptionUpdate;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionOptionModel;
import com.pocopi.api.repositories.FormQuestionOptionRepository;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FormOptionService {
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final ImageService imageService;

    public FormOptionService(
        FormQuestionOptionRepository formQuestionOptionRepository,
        ImageService imageService
    ) {
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.imageService = imageService;
    }

    @Transactional
    public void cloneOptions(int originalQuestionId, FormQuestionModel question) {
        final List<FormQuestionOptionModel> options = formQuestionOptionRepository
            .findAllByFormQuestionId(originalQuestionId);

        for (final FormQuestionOptionModel option : options) {
            final ImageModel newImage = option.getImage() != null ? imageService.cloneImage(option.getImage()) : null;

            final FormQuestionOptionModel newOption = FormQuestionOptionModel.builder()
                .formQuestion(question)
                .order(option.getOrder())
                .text(option.getText())
                .image(newImage)
                .build();

            formQuestionOptionRepository.save(newOption);
        }
    }

    @Transactional
    public boolean updateOptions(
        FormQuestionModel question,
        List<FormOptionUpdate> optionsUpdates,
        Map<Integer, FormQuestionOptionModel> storedOptionsMap,
        Map<Integer, Boolean> processedOptions,
        AtomicInteger imageIndex,
        List<MultipartFile> imageFiles
    ) {
        if (optionsUpdates == null || optionsUpdates.isEmpty()) {
            return true;
        }

        boolean modified = false;

        short order = 0;

        for (final FormOptionUpdate optionUpdate : optionsUpdates) {
            final MultipartFile optionImageFile = imageFiles.get(imageIndex.getAndIncrement());

            final boolean isNew = optionUpdate.id() == null
                || !storedOptionsMap.containsKey(optionUpdate.id());

            if (isNew) {
                final ImageModel optionImage = optionImageFile != null && !optionImageFile.isEmpty()
                    ? imageService.saveImageFile(ImageCategory.FORM_OPTION, optionImageFile, "Form option")
                    : null;

                final FormQuestionOptionModel newOption = FormQuestionOptionModel.builder()
                    .formQuestion(question)
                    .order(order++)
                    .text(optionUpdate.text())
                    .image(optionImage)
                    .build();

                formQuestionOptionRepository.save(newOption);
                modified = true;
                continue;
            }

            final int optionId = optionUpdate.id();
            final FormQuestionOptionModel storedOption = storedOptionsMap.get(optionId);

            processedOptions.put(optionId, true);

            final boolean updated = !Objects.equals(storedOption.getText(), optionUpdate.text())
                || storedOption.getOrder() != order
                || optionImageFile != null;

            if (!updated) {
                order++;
                continue;
            }

            final ImageModel storedImage = storedOption.getImage();

            storedOption.setText(optionUpdate.text());
            storedOption.setOrder(order++);

            if (optionImageFile != null) {
                if (optionImageFile.isEmpty()) {
                    storedOption.setImage(null);
                } else if (storedImage == null) {
                    final ImageModel newImage = imageService.saveImageFile(
                        ImageCategory.FORM_OPTION,
                        optionImageFile,
                        "Form option"
                    );
                    storedOption.setImage(newImage);
                } else {
                    imageService.updateImageFile(ImageCategory.FORM_OPTION, storedImage, optionImageFile);
                }
            }

            formQuestionOptionRepository.save(storedOption);

            if (storedImage != null && storedOption.getImage() == null) {
                imageService.deleteImageIfUnused(storedImage);
            }

            modified = true;
        }

        return modified;
    }
}
