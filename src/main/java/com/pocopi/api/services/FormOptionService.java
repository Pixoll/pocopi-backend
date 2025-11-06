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
    public boolean updateOptions(
        FormQuestionModel question,
        List<FormOptionUpdate> optionsUpdates,
        Map<Integer, FormQuestionOptionModel> storedOptionsMap,
        Map<Integer, Boolean> processedOptions,
        AtomicInteger imageIndex,
        Map<Integer, MultipartFile> imageFiles
    ) {
        if (optionsUpdates == null || optionsUpdates.isEmpty()) {
            return true;
        }

        boolean modified = false;

        byte order = 0;

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
                || storedOption.getOrder() != order++
                || optionImageFile != null;

            if (!updated) {
                continue;
            }

            final ImageModel storedImage = storedOption.getImage();

            storedOption.setText(optionUpdate.text());
            storedOption.setOrder(order);

            if (optionImageFile != null) {
                if (storedImage == null) {
                    final ImageModel newImage = imageService.saveImageFile(
                        ImageCategory.FORM_OPTION,
                        optionImageFile,
                        "Form option"
                    );
                    storedOption.setImage(newImage);
                } else if (!optionImageFile.isEmpty()) {
                    imageService.updateImageFile(storedOption.getImage(), optionImageFile);
                } else {
                    storedOption.setImage(null);
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
