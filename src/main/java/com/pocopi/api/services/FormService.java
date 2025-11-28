package com.pocopi.api.services;

import com.pocopi.api.dto.config.Image;
import com.pocopi.api.dto.form.*;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class FormService {
    private final FormRepository formRepository;
    private final FormQuestionService formQuestionService;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final FormQuestionSliderLabelRepository formQuestionSliderLabelRepository;
    private final UserFormSubmissionRepository userFormSubmissionRepository;
    private final UserFormAnswerRepository userFormAnswerRepository;
    private final ImageService imageService;

    public FormService(
        FormRepository formRepository,
        FormQuestionService formQuestionService,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository,
        FormQuestionSliderLabelRepository formQuestionSliderLabelRepository,
        UserFormSubmissionRepository userFormSubmissionRepository,
        UserFormAnswerRepository userFormAnswerRepository,
        ImageService imageService
    ) {
        this.formRepository = formRepository;
        this.formQuestionService = formQuestionService;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.formQuestionSliderLabelRepository = formQuestionSliderLabelRepository;
        this.userFormSubmissionRepository = userFormSubmissionRepository;
        this.userFormAnswerRepository = userFormAnswerRepository;
        this.imageService = imageService;
    }

    @Transactional
    public Map<FormType, Form> getFormsByConfigVersion(int configVersion) {
        final Map<FormType, Form> formsMap = formRepository.findAllByConfigVersion(configVersion).stream()
            .collect(Collectors.toMap(
                FormModel::getType,
                (form) -> new Form(form.getId(), new ArrayList<>()),
                (a, b) -> b
            ));

        if (formsMap.isEmpty()) {
            return formsMap;
        }

        final List<FormQuestionModel> questionsList = formQuestionRepository
            .findAllByFormConfigVersionOrderByOrder(configVersion);
        final List<FormQuestionOptionModel> optionsList = formQuestionOptionRepository
            .findAllByFormQuestionFormConfigVersionOrderByOrder(configVersion);
        final List<FormQuestionSliderLabelModel> sliderLabelsList = formQuestionSliderLabelRepository
            .findAllByFormQuestionFormConfigVersion(configVersion);

        final HashMap<Integer, FormQuestion> questionsMap = new HashMap<>();

        for (final FormQuestionModel questionModel : questionsList) {
            final FormQuestion question = parseFormQuestion(questionModel);
            questionsMap.put(questionModel.getId(), question);

            final Form form = formsMap.get(questionModel.getForm().getType());
            form.questions().add(question);
        }

        for (final FormQuestionOptionModel optionModel : optionsList) {
            final Image optionImage = optionModel.getImage() != null
                ? imageService.getImageById(optionModel.getImage().getId())
                : null;

            final FormOption option = new FormOption(optionModel.getId(), optionModel.getText(), optionImage);

            final FormQuestion question = questionsMap.get(optionModel.getFormQuestion().getId());

            switch (question) {
                case FormQuestion.SelectMultiple q -> q.options.add(option);
                case FormQuestion.SelectOne q -> q.options.add(option);
                default -> throw new IllegalArgumentException(
                    "Form question of type 'slider' and 'text-*' cannot have options"
                );
            }
        }

        for (final FormQuestionSliderLabelModel sliderLabelModel : sliderLabelsList) {
            final SliderLabel sliderLabel = new SliderLabel(
                sliderLabelModel.getId(),
                sliderLabelModel.getNumber(),
                sliderLabelModel.getLabel()
            );

            final FormQuestion question = questionsMap.get(sliderLabelModel.getFormQuestion().getId());

            if (question instanceof FormQuestion.Slider q) {
                q.labels.add(sliderLabel);
            } else {
                throw new IllegalArgumentException("Form question of type 'select-*' and 'text-*' cannot have options");
            }
        }

        return formsMap;
    }

    @Transactional
    public void cloneForms(int originalConfigVersion, ConfigModel config) {
        final List<FormModel> forms = formRepository.findAllByConfigVersion(originalConfigVersion);

        for (final FormModel form : forms) {
            final FormModel newForm = formRepository.save(FormModel.builder()
                .config(config)
                .type(form.getType())
                .title(form.getTitle())
                .build()
            );

            formQuestionService.cloneQuestions(form.getId(), newForm);
        }
    }

    @Transactional
    public boolean updateForm(
        ConfigModel config,
        FormType type,
        FormUpdate formUpdate,
        List<MultipartFile> imageFiles
    ) {
        final FormModel form = formRepository.findByTypeAndConfigVersion(type, config.getVersion()).orElse(null);

        if (form == null) {
            if (formUpdate == null) {
                return false;
            }

            final FormModel newForm = FormModel.builder()
                .config(config)
                .type(type)
                .title(formUpdate.title())
                .build();

            final FormModel savedForm = formRepository.save(newForm);

            final AtomicInteger imageIndex = new AtomicInteger(0);

            formQuestionService.updateQuestions(
                savedForm,
                formUpdate.questions(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                imageIndex,
                imageFiles
            );

            return true;
        }

        if (formUpdate == null && userFormSubmissionRepository.existsByFormId(form.getId())) {
            throw HttpException.conflict("Form of type " + type + " has user data related to it and cannot be deleted");
        }

        final AtomicBoolean modified = new AtomicBoolean(false);
        final FormModel savedForm;

        if (formUpdate != null && !Objects.equals(form.getTitle(), formUpdate.title())) {
            form.setTitle(formUpdate.title());

            savedForm = formRepository.save(form);
            modified.set(true);
        } else {
            savedForm = form;
        }

        final List<FormQuestionModel> storedQuestions = formQuestionRepository
            .findAllByFormIdOrderByOrder(savedForm.getId());
        final List<FormQuestionOptionModel> storedOptions = formQuestionOptionRepository
            .findAllByFormQuestionFormIdOrderByOrder(savedForm.getId());
        final List<FormQuestionSliderLabelModel> storedSliderLabels = formQuestionSliderLabelRepository
            .findAllByFormQuestionFormId(savedForm.getId());

        final Map<Integer, FormQuestionModel> storedQuestionsMap = new HashMap<>();
        final Map<Integer, FormQuestionOptionModel> storedOptionsMap = new HashMap<>();
        final Map<Integer, FormQuestionSliderLabelModel> storedSliderLabelsMap = new HashMap<>();

        final Map<Integer, Boolean> processedQuestions = new HashMap<>();
        final Map<Integer, Boolean> processedOptions = new HashMap<>();
        final Map<Integer, Boolean> processedSliderLabels = new HashMap<>();

        for (final FormQuestionModel question : storedQuestions) {
            storedQuestionsMap.put(question.getId(), question);
            processedQuestions.put(question.getId(), false);
        }

        for (final FormQuestionOptionModel option : storedOptions) {
            storedOptionsMap.put(option.getId(), option);
            processedOptions.put(option.getId(), false);
        }

        for (final FormQuestionSliderLabelModel sliderLabel : storedSliderLabels) {
            storedSliderLabelsMap.put(sliderLabel.getId(), sliderLabel);
            processedSliderLabels.put(sliderLabel.getId(), false);
        }

        final AtomicInteger imageIndex = new AtomicInteger(0);

        if (formUpdate != null) {
            final boolean modifiedQuestions = formQuestionService.updateQuestions(
                savedForm,
                formUpdate.questions(),
                storedQuestionsMap,
                storedOptionsMap,
                storedSliderLabelsMap,
                processedQuestions,
                processedOptions,
                processedSliderLabels,
                imageIndex,
                imageFiles
            );

            modified.set(modifiedQuestions || modified.get());
        }

        processedSliderLabels.forEach((sliderLabelId, processed) -> {
            if (processed) {
                return;
            }

            final FormQuestionSliderLabelModel sliderLabel = storedSliderLabelsMap.get(sliderLabelId);

            formQuestionSliderLabelRepository.delete(sliderLabel);
            modified.set(true);
        });

        processedOptions.forEach((optionId, processed) -> {
            if (processed) {
                return;
            }

            if (userFormAnswerRepository.existsByOptionId(optionId)) {
                throw HttpException.conflict(
                    "Form option with id " + optionId + " has user data related to it and cannot be deleted"
                );
            }

            final FormQuestionOptionModel option = storedOptionsMap.get(optionId);
            final ImageModel image = option.getImage();

            formQuestionOptionRepository.delete(option);

            if (image != null) {
                imageService.deleteImageIfUnused(image);
            }

            modified.set(true);
        });

        processedQuestions.forEach((questionId, processed) -> {
            if (processed) {
                return;
            }

            if (userFormAnswerRepository.existsByQuestionId(questionId)) {
                throw HttpException.conflict(
                    "Form question with id " + questionId + " in form of type " + type
                        + " has user data related to it and cannot be deleted"
                );
            }

            final FormQuestionModel question = storedQuestions.get(questionId);
            final ImageModel image = question.getImage();

            formQuestionRepository.delete(question);

            if (image != null) {
                imageService.deleteImageIfUnused(image);
            }

            modified.set(true);
        });

        if (formUpdate == null) {
            formRepository.delete(savedForm);
            modified.set(true);
        }

        return modified.get();
    }

    private FormQuestion parseFormQuestion(FormQuestionModel questionModel) {
        final Image questionImage = questionModel.getImage() != null
            ? imageService.getImageById(questionModel.getImage().getId())
            : null;

        final FormQuestion question;

        switch (questionModel.getType()) {
            case SELECT_MULTIPLE -> question = new FormQuestion.SelectMultiple(
                questionModel.getId(),
                questionModel.getCategory(),
                questionModel.getText(),
                questionImage,
                questionModel.getType(),
                new ArrayList<>(),
                questionModel.getMin(),
                questionModel.getMax(),
                questionModel.getOther()
            );

            case SELECT_ONE -> question = new FormQuestion.SelectOne(
                questionModel.getId(),
                questionModel.getCategory(),
                questionModel.getText(),
                questionImage,
                questionModel.getType(),
                new ArrayList<>(),
                questionModel.getOther()
            );

            case SLIDER -> question = new FormQuestion.Slider(
                questionModel.getId(),
                questionModel.getCategory(),
                questionModel.getText(),
                questionImage,
                questionModel.getType(),
                questionModel.getMin(),
                questionModel.getMax(),
                questionModel.getStep(),
                new ArrayList<>()
            );

            case TEXT_SHORT -> question = new FormQuestion.TextShort(
                questionModel.getId(),
                questionModel.getCategory(),
                questionModel.getText(),
                questionImage,
                questionModel.getType(),
                questionModel.getPlaceholder(),
                questionModel.getMinLength(),
                questionModel.getMaxLength()
            );

            case TEXT_LONG -> question = new FormQuestion.TextLong(
                questionModel.getId(),
                questionModel.getCategory(),
                questionModel.getText(),
                questionImage,
                questionModel.getType(),
                questionModel.getPlaceholder(),
                questionModel.getMinLength(),
                questionModel.getMaxLength()
            );

            default -> throw new IllegalArgumentException("Unknown form question type " + questionModel.getType());
        }

        return question;
    }
}
