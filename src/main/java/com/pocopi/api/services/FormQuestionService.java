package com.pocopi.api.services;

import com.pocopi.api.dto.form.FormQuestionUpdate;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.repositories.FormQuestionRepository;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FormQuestionService {
    private final FormQuestionRepository formQuestionRepository;
    private final FormOptionService formOptionService;
    private final ImageService imageService;
    private final FormSliderLabelService formSliderLabelService;

    public FormQuestionService(
        FormQuestionRepository formQuestionRepository,
        FormOptionService formOptionService,
        ImageService imageService,
        FormSliderLabelService formSliderLabelService
    ) {
        this.formQuestionRepository = formQuestionRepository;
        this.formOptionService = formOptionService;
        this.imageService = imageService;
        this.formSliderLabelService = formSliderLabelService;
    }

    @Transactional
    public boolean updateQuestions(
        FormModel form,
        List<FormQuestionUpdate> formQuestionsUpdates,
        Map<Integer, FormQuestionModel> storedQuestionsMap,
        Map<Integer, FormQuestionOptionModel> storedOptionsMap,
        Map<Integer, FormQuestionSliderLabelModel> storedSliderLabelsMap,
        Map<Integer, Boolean> processedQuestions,
        Map<Integer, Boolean> processedOptions,
        Map<Integer, Boolean> processedSliderLabels,
        AtomicInteger imageIndex,
        List<MultipartFile> imageFiles
    ) {
        if (formQuestionsUpdates == null || formQuestionsUpdates.isEmpty()) {
            return true;
        }

        boolean modified = false;

        byte order = 0;

        for (final FormQuestionUpdate formQuestionUpdate : formQuestionsUpdates) {
            final MultipartFile questionImageFile = imageFiles.get(imageIndex.getAndIncrement());

            switch (formQuestionUpdate) {
                case FormQuestionUpdate.SelectMultipleUpdate questionUpdate -> {
                    final boolean isNew = questionUpdate.id() == null
                        || !storedQuestionsMap.containsKey(questionUpdate.id());

                    if (isNew) {
                        final ImageModel questionImage = questionImageFile != null && !questionImageFile.isEmpty()
                            ? imageService.saveImageFile(
                            ImageCategory.FORM_QUESTION,
                            questionImageFile,
                            "Form question"
                        )
                            : null;

                        final FormQuestionModel newQuestion = FormQuestionModel.builder()
                            .form(form)
                            .order(order++)
                            .category(questionUpdate.category())
                            .text(questionUpdate.text())
                            .image(questionImage)
                            .required(true)
                            .type(FormQuestionType.SELECT_MULTIPLE)
                            .min(questionUpdate.min())
                            .max(questionUpdate.max())
                            .other(questionUpdate.other())
                            .build();

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        formOptionService.updateOptions(
                            savedNewQuestion,
                            questionUpdate.options(),
                            storedOptionsMap,
                            processedOptions,
                            imageIndex,
                            imageFiles
                        );

                        modified = true;
                        continue;
                    }

                    final Integer questionId = questionUpdate.id();
                    final FormQuestionModel storedQuestion = storedQuestionsMap.get(questionId);

                    processedQuestions.put(questionId, true);

                    final boolean updated = !Objects.equals(storedQuestion.getCategory(), questionUpdate.category())
                        || !Objects.equals(storedQuestion.getText(), questionUpdate.text())
                        || storedQuestion.getMin() != questionUpdate.min()
                        || storedQuestion.getMax() != questionUpdate.max()
                        || storedQuestion.getOther() != questionUpdate.other()
                        || storedQuestion.getOrder() != order
                        || questionImageFile != null;

                    if (!updated) {
                        final boolean modifiedOptions = formOptionService.updateOptions(
                            storedQuestion,
                            questionUpdate.options(),
                            storedOptionsMap,
                            processedOptions,
                            imageIndex,
                            imageFiles
                        );

                        modified = modifiedOptions || modified;
                        order++;
                        continue;
                    }

                    final ImageModel storedImage = storedQuestion.getImage();

                    storedQuestion.setCategory(questionUpdate.category());
                    storedQuestion.setText(questionUpdate.text());
                    storedQuestion.setMax(questionUpdate.max());
                    storedQuestion.setMin(questionUpdate.min());
                    storedQuestion.setOther(questionUpdate.other());
                    storedQuestion.setOrder(order++);

                    if (questionImageFile != null) {
                        if (questionImageFile.isEmpty()) {
                            storedQuestion.setImage(null);
                        } else if (storedImage == null) {
                            final ImageModel newImage = imageService.saveImageFile(
                                ImageCategory.FORM_QUESTION,
                                questionImageFile,
                                "Form question"
                            );
                            storedQuestion.setImage(newImage);
                        } else {
                            imageService.updateImageFile(ImageCategory.FORM_QUESTION, storedImage, questionImageFile);
                        }
                    }

                    final FormQuestionModel updatedQuestion = formQuestionRepository.save(storedQuestion);

                    if (storedImage != null && storedQuestion.getImage() == null) {
                        imageService.deleteImageIfUnused(storedImage);
                    }

                    formOptionService.updateOptions(
                        updatedQuestion,
                        questionUpdate.options(),
                        storedOptionsMap,
                        processedOptions,
                        imageIndex,
                        imageFiles
                    );

                    modified = true;
                }

                case FormQuestionUpdate.SelectOneUpdate questionUpdate -> {
                    final boolean isNew = questionUpdate.id() == null
                        || !storedQuestionsMap.containsKey(questionUpdate.id());

                    if (isNew) {
                        final ImageModel questionImage = questionImageFile != null && !questionImageFile.isEmpty()
                            ? imageService.saveImageFile(
                            ImageCategory.FORM_QUESTION,
                            questionImageFile,
                            "Form question"
                        )
                            : null;

                        final FormQuestionModel newQuestion = FormQuestionModel.builder()
                            .form(form)
                            .order(order++)
                            .category(questionUpdate.category())
                            .text(questionUpdate.text())
                            .image(questionImage)
                            .required(true)
                            .type(FormQuestionType.SELECT_ONE)
                            .other(questionUpdate.other())
                            .build();

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        formOptionService.updateOptions(
                            savedNewQuestion,
                            questionUpdate.options(),
                            storedOptionsMap,
                            processedOptions,
                            imageIndex,
                            imageFiles
                        );

                        modified = true;
                        continue;
                    }

                    final Integer questionId = questionUpdate.id();
                    final FormQuestionModel storedQuestion = storedQuestionsMap.get(questionId);

                    processedQuestions.put(questionId, true);

                    final boolean updated = !Objects.equals(storedQuestion.getCategory(), questionUpdate.category())
                        || !Objects.equals(storedQuestion.getText(), questionUpdate.text())
                        || storedQuestion.getOther() != questionUpdate.other()
                        || storedQuestion.getOrder() != order
                        || questionImageFile != null;

                    if (!updated) {
                        final boolean modifiedOptions = formOptionService.updateOptions(
                            storedQuestion,
                            questionUpdate.options(),
                            storedOptionsMap,
                            processedOptions,
                            imageIndex,
                            imageFiles
                        );

                        modified = modifiedOptions || modified;
                        order++;
                        continue;
                    }

                    final ImageModel storedImage = storedQuestion.getImage();

                    storedQuestion.setCategory(questionUpdate.category());
                    storedQuestion.setText(questionUpdate.text());
                    storedQuestion.setOther(questionUpdate.other());
                    storedQuestion.setOrder(order++);

                    if (questionImageFile != null) {
                        if (questionImageFile.isEmpty()) {
                            storedQuestion.setImage(null);
                        } else if (storedImage == null) {
                            final ImageModel newImage = imageService.saveImageFile(
                                ImageCategory.FORM_QUESTION,
                                questionImageFile,
                                "Form question"
                            );
                            storedQuestion.setImage(newImage);
                        } else {
                            imageService.updateImageFile(ImageCategory.FORM_QUESTION, storedImage, questionImageFile);
                        }
                    }

                    final FormQuestionModel updatedQuestion = formQuestionRepository.save(storedQuestion);

                    if (storedImage != null && storedQuestion.getImage() == null) {
                        imageService.deleteImageIfUnused(storedImage);
                    }

                    formOptionService.updateOptions(
                        updatedQuestion,
                        questionUpdate.options(),
                        storedOptionsMap,
                        processedOptions,
                        imageIndex,
                        imageFiles
                    );

                    modified = true;
                }

                case FormQuestionUpdate.SliderUpdate questionUpdate -> {
                    final boolean isNew = questionUpdate.id() == null
                        || !storedQuestionsMap.containsKey(questionUpdate.id());

                    if (isNew) {
                        final ImageModel questionImage = questionImageFile != null && !questionImageFile.isEmpty()
                            ? imageService.saveImageFile(
                            ImageCategory.FORM_QUESTION,
                            questionImageFile,
                            "Form question"
                        )
                            : null;

                        final FormQuestionModel newQuestion = FormQuestionModel.builder()
                            .form(form)
                            .order(order++)
                            .category(questionUpdate.category())
                            .text(questionUpdate.text())
                            .image(questionImage)
                            .required(true)
                            .type(FormQuestionType.SLIDER)
                            .min(questionUpdate.min())
                            .max(questionUpdate.max())
                            .step(questionUpdate.step())
                            .build();

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        formSliderLabelService.updateSliderLabels(
                            savedNewQuestion,
                            questionUpdate.labels(),
                            storedSliderLabelsMap,
                            processedSliderLabels
                        );

                        modified = true;
                        continue;
                    }

                    final Integer questionId = questionUpdate.id();
                    final FormQuestionModel storedQuestion = storedQuestionsMap.get(questionId);

                    processedQuestions.put(questionId, true);

                    final boolean updated = !Objects.equals(storedQuestion.getCategory(), questionUpdate.category())
                        || !Objects.equals(storedQuestion.getText(), questionUpdate.text())
                        || storedQuestion.getMin() != questionUpdate.min()
                        || storedQuestion.getMax() != questionUpdate.max()
                        || storedQuestion.getStep() != questionUpdate.step()
                        || storedQuestion.getOrder() != order
                        || questionImageFile != null;

                    if (!updated) {
                        final boolean modifiedSliderLabels = formSliderLabelService.updateSliderLabels(
                            storedQuestion,
                            questionUpdate.labels(),
                            storedSliderLabelsMap,
                            processedSliderLabels
                        );

                        modified = modifiedSliderLabels || modified;
                        order++;
                        continue;
                    }

                    final ImageModel storedImage = storedQuestion.getImage();

                    storedQuestion.setCategory(questionUpdate.category());
                    storedQuestion.setText(questionUpdate.text());
                    storedQuestion.setMax(questionUpdate.max());
                    storedQuestion.setMin(questionUpdate.min());
                    storedQuestion.setStep(questionUpdate.step());
                    storedQuestion.setOrder(order++);

                    if (questionImageFile != null) {
                        if (questionImageFile.isEmpty()) {
                            storedQuestion.setImage(null);
                        } else if (storedImage == null) {
                            final ImageModel newImage = imageService.saveImageFile(
                                ImageCategory.FORM_QUESTION,
                                questionImageFile,
                                "Form question"
                            );
                            storedQuestion.setImage(newImage);
                        } else {
                            imageService.updateImageFile(ImageCategory.FORM_QUESTION, storedImage, questionImageFile);
                        }
                    }

                    final FormQuestionModel updatedQuestion = formQuestionRepository.save(storedQuestion);

                    if (storedImage != null && storedQuestion.getImage() == null) {
                        imageService.deleteImageIfUnused(storedImage);
                    }

                    formSliderLabelService.updateSliderLabels(
                        updatedQuestion,
                        questionUpdate.labels(),
                        storedSliderLabelsMap,
                        processedSliderLabels
                    );

                    modified = true;
                }

                case FormQuestionUpdate.TextShortUpdate questionUpdate -> {
                    final boolean isNew = questionUpdate.id() == null
                        || !storedQuestionsMap.containsKey(questionUpdate.id());

                    if (isNew) {
                        final ImageModel questionImage = questionImageFile != null && !questionImageFile.isEmpty()
                            ? imageService.saveImageFile(
                            ImageCategory.FORM_QUESTION,
                            questionImageFile,
                            "Form question"
                        )
                            : null;

                        final FormQuestionModel newQuestion = FormQuestionModel.builder()
                            .form(form)
                            .order(order++)
                            .category(questionUpdate.category())
                            .text(questionUpdate.text())
                            .image(questionImage)
                            .required(true)
                            .type(FormQuestionType.TEXT_SHORT)
                            .minLength(questionUpdate.minLength())
                            .maxLength(questionUpdate.maxLength())
                            .placeholder(questionUpdate.placeholder())
                            .build();

                        formQuestionRepository.save(newQuestion);
                        modified = true;
                        continue;
                    }

                    final Integer questionId = questionUpdate.id();
                    final FormQuestionModel storedQuestion = storedQuestionsMap.get(questionId);

                    processedQuestions.put(questionId, true);

                    final boolean updated = !Objects.equals(storedQuestion.getCategory(), questionUpdate.category())
                        || !Objects.equals(storedQuestion.getText(), questionUpdate.text())
                        || !Objects.equals(storedQuestion.getPlaceholder(), questionUpdate.placeholder())
                        || storedQuestion.getMinLength() != questionUpdate.minLength()
                        || storedQuestion.getMaxLength() != questionUpdate.maxLength()
                        || storedQuestion.getOrder() != order
                        || questionImageFile != null;

                    if (!updated) {
                        order++;
                        continue;
                    }

                    final ImageModel storedImage = storedQuestion.getImage();

                    storedQuestion.setCategory(questionUpdate.category());
                    storedQuestion.setText(questionUpdate.text());
                    storedQuestion.setMaxLength(questionUpdate.maxLength());
                    storedQuestion.setMinLength(questionUpdate.minLength());
                    storedQuestion.setPlaceholder(questionUpdate.placeholder());
                    storedQuestion.setOrder(order++);

                    if (questionImageFile != null) {
                        if (questionImageFile.isEmpty()) {
                            storedQuestion.setImage(null);
                        } else if (storedImage == null) {
                            final ImageModel newImage = imageService.saveImageFile(
                                ImageCategory.FORM_QUESTION,
                                questionImageFile,
                                "Form question"
                            );
                            storedQuestion.setImage(newImage);
                        } else {
                            imageService.updateImageFile(ImageCategory.FORM_QUESTION, storedImage, questionImageFile);
                        }
                    }

                    formQuestionRepository.save(storedQuestion);

                    if (storedImage != null && storedQuestion.getImage() == null) {
                        imageService.deleteImageIfUnused(storedImage);
                    }

                    modified = true;
                }

                case FormQuestionUpdate.TextLongUpdate questionUpdate -> {
                    final boolean isNew = questionUpdate.id() == null
                        || !storedQuestionsMap.containsKey(questionUpdate.id());

                    if (isNew) {
                        final ImageModel questionImage = questionImageFile != null && !questionImageFile.isEmpty()
                            ? imageService.saveImageFile(
                            ImageCategory.FORM_QUESTION,
                            questionImageFile,
                            "Form question"
                        )
                            : null;

                        final FormQuestionModel newQuestion = FormQuestionModel.builder()
                            .form(form)
                            .order(order++)
                            .category(questionUpdate.category())
                            .text(questionUpdate.text())
                            .image(questionImage)
                            .required(true)
                            .type(FormQuestionType.TEXT_LONG)
                            .minLength(questionUpdate.minLength())
                            .maxLength(questionUpdate.maxLength())
                            .placeholder(questionUpdate.placeholder())
                            .build();

                        formQuestionRepository.save(newQuestion);
                        modified = true;
                        continue;
                    }

                    final Integer questionId = questionUpdate.id();
                    final FormQuestionModel storedQuestion = storedQuestionsMap.get(questionId);

                    processedQuestions.put(questionId, true);

                    final boolean updated = !Objects.equals(storedQuestion.getCategory(), questionUpdate.category())
                        || !Objects.equals(storedQuestion.getText(), questionUpdate.text())
                        || !Objects.equals(storedQuestion.getPlaceholder(), questionUpdate.placeholder())
                        || storedQuestion.getMinLength() != questionUpdate.minLength()
                        || storedQuestion.getMaxLength() != questionUpdate.maxLength()
                        || storedQuestion.getOrder() != order
                        || questionImageFile != null;

                    if (!updated) {
                        order++;
                        continue;
                    }

                    final ImageModel storedImage = storedQuestion.getImage();

                    storedQuestion.setCategory(questionUpdate.category());
                    storedQuestion.setText(questionUpdate.text());
                    storedQuestion.setMaxLength(questionUpdate.maxLength());
                    storedQuestion.setMinLength(questionUpdate.minLength());
                    storedQuestion.setPlaceholder(questionUpdate.placeholder());
                    storedQuestion.setOrder(order++);

                    if (questionImageFile != null) {
                        if (questionImageFile.isEmpty()) {
                            storedQuestion.setImage(null);
                        } else if (storedImage == null) {
                            final ImageModel newImage = imageService.saveImageFile(
                                ImageCategory.FORM_QUESTION,
                                questionImageFile,
                                "Form question"
                            );
                            storedQuestion.setImage(newImage);
                        } else {
                            imageService.updateImageFile(ImageCategory.FORM_QUESTION, storedImage, questionImageFile);
                        }
                    }

                    formQuestionRepository.save(storedQuestion);

                    if (storedImage != null && storedQuestion.getImage() == null) {
                        imageService.deleteImageIfUnused(storedImage);
                    }

                    modified = true;
                }
            }
        }

        return modified;
    }
}
