package com.pocopi.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.config.*;
import com.pocopi.api.dto.form.Form;
import com.pocopi.api.dto.form.FormOptionUpdate;
import com.pocopi.api.dto.form.FormQuestionUpdate;
import com.pocopi.api.dto.form.FormUpdate;
import com.pocopi.api.dto.config.Image;
import com.pocopi.api.dto.test.TestGroup;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionOptionModel;
import com.pocopi.api.models.form.FormQuestionType;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ConfigService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ConfigRepository configRepository;
    private final TranslationValueRepository translationValueRepository;
    private final FormService formService;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final HomeFaqService homeFaqService;
    private final HomeInfoCardService homeInfoCardService;
    private final ImageService imageService;
    private final TestGroupService testGroupService;
    private final ImageRepository imageRepository;
    private final HomeInfoCardRepository homeInfoCardRepository;
    private final HomeFaqRepository homeFaqRepository;

    public ConfigService(
        ConfigRepository configRepository,
        TranslationValueRepository translationValueRepository,
        FormService formService,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository,
        HomeFaqService homeFaqService,
        HomeFaqRepository homeFaqRepository,
        HomeInfoCardService homeInfoCardService,
        HomeInfoCardRepository homeInfoCardRepository,
        ImageService imageService,
        ImageRepository imageRepository,
        TestGroupService testGroupService
    ) {
        this.configRepository = configRepository;
        this.translationValueRepository = translationValueRepository;
        this.formService = formService;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.homeFaqService = homeFaqService;
        this.homeFaqRepository = homeFaqRepository;
        this.homeInfoCardService = homeInfoCardService;
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.testGroupService = testGroupService;
    }

    @Transactional
    public TrimmedConfig getLatestConfigTrimmed() {
        final ConfigModel configModel = configRepository.findLastConfig();
        final int configVersion = configModel.getVersion();

        final Image icon = configModel.getIcon() != null
            ? imageService.getImageById(configModel.getIcon().getId())
            : null;

        final Map<String, String> translations = translationValueRepository
            .findAllByConfigVersion(configVersion)
            .stream()
            .collect(
                HashMap::new,
                (map, translation) -> map.put(translation.getKey(), translation.getValue()),
                HashMap::putAll
            );

        final Map<FormType, Form> forms = formService.getFormsByConfigVersion(configVersion);
        final Form preTest = forms.get(FormType.PRE);
        final Form postTest = forms.get(FormType.POST);

        final List<InformationCard> informationCards = getInformationCards(configVersion);
        final List<FrequentlyAskedQuestion> frequentlyAskedQuestions = getFrequentlyAskedQuestions(configVersion);

        return new TrimmedConfig(
            icon,
            configModel.getTitle(),
            configModel.getSubtitle(),
            configModel.getDescription(),
            configModel.isAnonymous(),
            informationCards,
            configModel.getInformedConsent(),
            frequentlyAskedQuestions,
            preTest,
            postTest,
            translations
        );
    }

    @Transactional
    public FullConfig getLatestConfigFull() {
        final ConfigModel configModel = configRepository.findLastConfig();
        final int configVersion = configModel.getVersion();

        final Image icon = configModel.getIcon() != null
            ? imageService.getImageById(configModel.getIcon().getId())
            : null;

        final List<Translation> translations = translationValueRepository
            .findAllByConfigVersionWithDetails(configVersion)
            .stream()
            .map((translation) -> new Translation(
                translation.getKey(),
                translation.getValue(),
                translation.getDescription(),
                parseJsonStringArray(translation.getArgumentsJson())
            ))
            .toList();

        final Map<FormType, Form> forms = formService.getFormsByConfigVersion(configVersion);
        final Form preTest = forms.get(FormType.PRE);
        final Form postTest = forms.get(FormType.POST);

        final List<TestGroup> groups = testGroupService.getGroupsByConfigVersion(configVersion);

        final List<InformationCard> informationCards = getInformationCards(configVersion);
        final List<FrequentlyAskedQuestion> frequentlyAskedQuestions = getFrequentlyAskedQuestions(configVersion);

        return new FullConfig(
            configVersion,
            icon,
            configModel.getTitle(),
            configModel.getSubtitle(),
            configModel.getDescription(),
            configModel.isAnonymous(),
            informationCards,
            configModel.getInformedConsent(),
            frequentlyAskedQuestions,
            preTest,
            postTest,
            groups,
            translations
        );
    }

    public UpdatedConfig updateConfig(ConfigUpdateWithFiles request) {
        final ConfigModel savedModel = configRepository.getByVersion(request.updateLastConfig().version());

        final Map<Integer, File> preTestFiles = convertMultipartFileMap(request.preTestFormQuestionOptionsFiles());
        final Map<Integer, File> postTestFiles = convertMultipartFileMap(request.postTestFormQuestionOptionsFiles());
        final Map<Integer, File> groupFiles = convertMultipartFileMap(request.groupQuestionOptionsFiles());
        final Map<Integer, File> infoCardFiles = convertMultipartFileMap(request.informationCardFiles());

        final Map<String, String> configUpdatesSummary = processConfigGeneralData(
            savedModel,
            request.updateLastConfig()
        );
        final Map<String, String> informationCardUpdatesSummary = homeInfoCardService.processCardInformation(
            request.updateLastConfig().informationCards(),
            infoCardFiles
        );
        final Map<String, String> faqUpdatedSummary = homeFaqService.processFaq(request.updateLastConfig().faq());
        final Map<String, String> preTestUpdatedSummary = processFormQuestions(
            request.updateLastConfig().preTestForm(),
            preTestFiles
        );
        final Map<String, String> postTestUpdatedSummary = processFormQuestions(
            request.updateLastConfig().postTestForm(),
            postTestFiles
        );
        final Map<String, String> groupSummary = testGroupService.processGroups(
            request.updateLastConfig().groups(),
            groupFiles
        );

        return new UpdatedConfig(
            configUpdatesSummary,
            informationCardUpdatesSummary,
            faqUpdatedSummary,
            preTestUpdatedSummary,
            postTestUpdatedSummary,
            groupSummary
        );
    }

    private List<FrequentlyAskedQuestion> getFrequentlyAskedQuestions(int configVersion) {
        return homeFaqRepository
            .findAllByConfigVersion(configVersion)
            .stream()
            .map(faq -> new FrequentlyAskedQuestion(faq.getId(), faq.getQuestion(), faq.getAnswer()))
            .collect(Collectors.toList());
    }

    private List<InformationCard> getInformationCards(int configVersion) {
        return homeInfoCardRepository
            .findAllByConfigVersion(configVersion)
            .stream()
            .map(card -> {
                final Image iconByInfoCard = card.getIcon() != null
                    ? imageService.getImageById(card.getIcon().getId())
                    : null;

                return new InformationCard(
                    card.getId(),
                    card.getTitle(),
                    card.getDescription(),
                    card.getColor(),
                    iconByInfoCard
                );
            })
            .collect(Collectors.toList());
    }

    private Map<String, String> processFormQuestions(FormUpdate updatedForm, Map<Integer, File> images) {
        final Map<String, String> results = new HashMap<>();
        final List<FormQuestionModel> allExistingQuestions = formQuestionRepository.findAll();
        final Map<Integer, Boolean> processedQuestions = new HashMap<>();

        for (final FormQuestionModel question : allExistingQuestions) {
            processedQuestions.put(question.getId(), false);
        }

        int order = 0;
        int imageIndex = 0;

        for (final FormQuestionUpdate patchQuestion : updatedForm.questions()) {
            switch (patchQuestion) {
                case FormQuestionUpdate.SelectMultipleUpdate selectMultiple -> {
                    if (selectMultiple.id.isPresent()) {
                        final Integer questionId = selectMultiple.id.get();
                        final FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(
                            questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        final boolean infoChanged = checkChangeByQuestion(savedQuestion, selectMultiple);
                        final boolean orderChanged = savedQuestion.getOrder() != order;

                        final boolean hasImageChange = questionImage != null;
                        final boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        final boolean replaceImage = hasImageChange && questionImage.length() > 0;

                        if (infoChanged || orderChanged || deleteImage || replaceImage) {
                            savedQuestion.setCategory(selectMultiple.category);
                            savedQuestion.setText(selectMultiple.text.orElse(null));
                            savedQuestion.setOrder((byte) order);
                            savedQuestion.setMax((short) selectMultiple.max);
                            savedQuestion.setMin((short) selectMultiple.min);
                            savedQuestion.setOther(selectMultiple.other);

                            if (deleteImage) {
                                deleteImageFromEntity(savedQuestion);
                            } else if (replaceImage) {
                                updateOrCreateQuestionImage(savedQuestion, questionImage);
                            }

                            formQuestionRepository.save(savedQuestion);
                            results.put("question_" + questionId, "Updated successfully");
                        } else {
                            results.put("question_" + questionId, "No changes");
                        }

                        imageIndex = processQuestionOptions(
                            savedQuestion,
                            selectMultiple.options,
                            images,
                            imageIndex,
                            results
                        );
                        processedQuestions.put(questionId, true);
                    } else {
                        // Nueva pregunta
                        final FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(selectMultiple.category);
                        newQuestion.setText(selectMultiple.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setMax((short) selectMultiple.max);
                        newQuestion.setMin((short) selectMultiple.min);
                        newQuestion.setType(FormQuestionType.SELECT_MULTIPLE);
                        newQuestion.setOther(selectMultiple.other);

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        imageIndex = processQuestionOptions(
                            savedNewQuestion,
                            selectMultiple.options,
                            images,
                            imageIndex,
                            results
                        );
                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case FormQuestionUpdate.SelectOneUpdate selectOne -> {
                    if (selectOne.id.isPresent()) {
                        final Integer questionId = selectOne.id.get();
                        final FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(
                            questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        final boolean infoChanged = checkChangeByQuestion(savedQuestion, selectOne);
                        final boolean orderChanged = savedQuestion.getOrder() != order;
                        final boolean hasImageChange = questionImage != null;
                        final boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        final boolean replaceImage = hasImageChange && questionImage.length() > 0;

                        if (infoChanged || orderChanged || deleteImage || replaceImage) {
                            savedQuestion.setCategory(selectOne.category);
                            savedQuestion.setText(selectOne.text.orElse(null));
                            savedQuestion.setOrder((byte) order);
                            savedQuestion.setOther(selectOne.other);

                            if (deleteImage) {
                                deleteImageFromEntity(savedQuestion);
                            } else if (replaceImage) {
                                updateOrCreateQuestionImage(savedQuestion, questionImage);
                            }

                            formQuestionRepository.save(savedQuestion);
                            results.put("question_" + questionId, "Updated successfully");
                        } else {
                            results.put("question_" + questionId, "No changes");
                        }

                        imageIndex = processQuestionOptions(
                            savedQuestion,
                            selectOne.options,
                            images,
                            imageIndex,
                            results
                        );
                        processedQuestions.put(questionId, true);
                    } else {
                        final FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(selectOne.category);
                        newQuestion.setText(selectOne.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.SELECT_ONE);
                        newQuestion.setOther(selectOne.other);

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        imageIndex = processQuestionOptions(
                            savedNewQuestion,
                            selectOne.options,
                            images,
                            imageIndex,
                            results
                        );
                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case FormQuestionUpdate.SliderUpdate slider -> {
                    if (slider.id.isPresent()) {
                        final Integer questionId = slider.id.get();
                        final FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(
                            questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        final boolean infoChanged = checkChangeByQuestion(savedQuestion, slider);
                        final boolean orderChanged = savedQuestion.getOrder() != order;
                        final boolean hasImageChange = questionImage != null;
                        final boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        final boolean replaceImage = hasImageChange && questionImage.length() > 0;

                        if (infoChanged || orderChanged || deleteImage || replaceImage) {
                            savedQuestion.setCategory(slider.category);
                            savedQuestion.setText(slider.text.orElse(null));
                            savedQuestion.setOrder((byte) order);
                            savedQuestion.setPlaceholder(slider.placeholder);
                            savedQuestion.setMin((short) slider.min);
                            savedQuestion.setMax((short) slider.max);
                            savedQuestion.setStep((short) slider.step);

                            if (deleteImage) {
                                deleteImageFromEntity(savedQuestion);
                            } else if (replaceImage) {
                                updateOrCreateQuestionImage(savedQuestion, questionImage);
                            }

                            formQuestionRepository.save(savedQuestion);
                            results.put("question_" + questionId, "Updated successfully");
                        } else {
                            results.put("question_" + questionId, "No changes");
                        }

                        processedQuestions.put(questionId, true);
                    } else {
                        final FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(slider.category);
                        newQuestion.setText(slider.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.SLIDER);
                        newQuestion.setPlaceholder(slider.placeholder);
                        newQuestion.setMin((short) slider.min);
                        newQuestion.setMax((short) slider.max);
                        newQuestion.setStep((short) slider.step);

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case FormQuestionUpdate.TextLongUpdate textLong -> {
                    if (textLong.id.isPresent()) {
                        final Integer questionId = textLong.id.get();
                        final FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(
                            questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        final boolean infoChanged = checkChangeByQuestion(savedQuestion, textLong);
                        final boolean orderChanged = savedQuestion.getOrder() != order;
                        final boolean hasImageChange = questionImage != null;
                        final boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        final boolean replaceImage = hasImageChange && questionImage.length() > 0;

                        if (infoChanged || orderChanged || deleteImage || replaceImage) {
                            savedQuestion.setCategory(textLong.category);
                            savedQuestion.setText(textLong.text.orElse(null));
                            savedQuestion.setOrder((byte) order);
                            savedQuestion.setPlaceholder(textLong.placeholder);
                            savedQuestion.setMinLength((short) textLong.minLength);
                            savedQuestion.setMaxLength((short) textLong.maxLength);

                            if (deleteImage) {
                                deleteImageFromEntity(savedQuestion);
                            } else if (replaceImage) {
                                updateOrCreateQuestionImage(savedQuestion, questionImage);
                            }

                            formQuestionRepository.save(savedQuestion);
                            results.put("question_" + questionId, "Updated successfully");
                        } else {
                            results.put("question_" + questionId, "No changes");
                        }

                        processedQuestions.put(questionId, true);
                    } else {
                        final FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(textLong.category);
                        newQuestion.setText(textLong.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.TEXT_LONG);
                        newQuestion.setPlaceholder(textLong.placeholder);
                        newQuestion.setMinLength((short) textLong.minLength);
                        newQuestion.setMaxLength((short) textLong.maxLength);

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case FormQuestionUpdate.TextShortUpdate textShort -> {
                    if (textShort.id.isPresent()) {
                        final Integer questionId = textShort.id.get();
                        final FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(
                            questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        final boolean infoChanged = checkChangeByQuestion(savedQuestion, textShort);
                        final boolean orderChanged = savedQuestion.getOrder() != order;
                        final boolean hasImageChange = questionImage != null;
                        final boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        final boolean replaceImage = hasImageChange && questionImage.length() > 0;

                        if (infoChanged || orderChanged || deleteImage || replaceImage) {
                            savedQuestion.setCategory(textShort.category);
                            savedQuestion.setText(textShort.text.orElse(null));
                            savedQuestion.setOrder((byte) order);
                            savedQuestion.setPlaceholder(textShort.placeholder);
                            savedQuestion.setMinLength((short) textShort.minLength);
                            savedQuestion.setMaxLength((short) textShort.maxLength);

                            if (deleteImage) {
                                deleteImageFromEntity(savedQuestion);
                            } else if (replaceImage) {
                                updateOrCreateQuestionImage(savedQuestion, questionImage);
                            }

                            formQuestionRepository.save(savedQuestion);
                            results.put("question_" + questionId, "Updated successfully");
                        } else {
                            results.put("question_" + questionId, "No changes");
                        }

                        processedQuestions.put(questionId, true);
                    } else {
                        final FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(textShort.category);
                        newQuestion.setText(textShort.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.TEXT_SHORT);
                        newQuestion.setPlaceholder(textShort.placeholder);
                        newQuestion.setMinLength((short) textShort.minLength);
                        newQuestion.setMaxLength((short) textShort.maxLength);

                        final FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        final File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }
            }

            order++;
        }

        for (final Map.Entry<Integer, Boolean> entry : processedQuestions.entrySet()) {
            if (!entry.getValue()) {
                final FormQuestionModel questionToDelete = formQuestionRepository.findById(entry.getKey()).orElse(null);
                if (questionToDelete != null) {
                    deleteQuestionWithImage(questionToDelete);
                    results.put("question_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private int processQuestionOptions(
        FormQuestionModel question,
        List<FormOptionUpdate> patchOptions,
        Map<Integer, File> images,
        int currentImageIndex,
        Map<String, String> results
    ) {
        final List<FormQuestionOptionModel> allExistingOptions = formQuestionOptionRepository.findAllByFormQuestion_Id(
            question.getId());
        final Map<Integer, Boolean> processedOptions = new HashMap<>();

        for (final FormQuestionOptionModel option : allExistingOptions) {
            processedOptions.put(option.getId(), false);
        }

        int optionOrder = 0;
        int imageIndex = currentImageIndex;

        for (final FormOptionUpdate patchOption : patchOptions) {
            if (patchOption.id().isPresent()) {
                final Integer optionId = patchOption.id().get();
                final FormQuestionOptionModel savedOption = formQuestionOptionRepository.getFormQuestionOptionModelById(
                    optionId);

                if (savedOption == null) {
                    results.put("question_" + question.getId() + "_option_" + optionId, "Option not found");
                    optionOrder++;
                    imageIndex++;
                    continue;
                }

                final File optionImage = images.get(imageIndex);
                imageIndex++;

                final boolean textChanged = !Objects.equals(savedOption.getText(), patchOption.text().orElse(null));
                final boolean orderChanged = savedOption.getOrder() != optionOrder;
                final boolean hasImageChange = optionImage != null;
                final boolean deleteImage = hasImageChange && optionImage.length() == 0;
                final boolean replaceImage = hasImageChange && optionImage.length() > 0;

                if (textChanged || orderChanged || deleteImage || replaceImage) {
                    savedOption.setText(patchOption.text().orElse(null));
                    savedOption.setOrder((byte) optionOrder);

                    if (deleteImage) {
                        deleteImageFromEntity(savedOption);
                    } else if (replaceImage) {
                        updateOrCreateOptionImage(savedOption, optionImage);
                    }

                    formQuestionOptionRepository.save(savedOption);
                    results.put("question_" + question.getId() + "_option_" + optionId, "Updated successfully");
                } else {
                    results.put("question_" + question.getId() + "_option_" + optionId, "No changes");
                }

                processedOptions.put(optionId, true);
            } else {
                final FormQuestionOptionModel newOption = new FormQuestionOptionModel();
                newOption.setText(patchOption.text().orElse(null));
                newOption.setOrder((byte) optionOrder);
                newOption.setFormQuestion(question);

                final FormQuestionOptionModel savedNewOption = formQuestionOptionRepository.save(newOption);

                final File optionImage = images.get(imageIndex);
                imageIndex++;

                if (optionImage != null && optionImage.length() > 0) {
                    updateOrCreateOptionImage(savedNewOption, optionImage);
                }

                results.put(
                    "question_" + question.getId() + "_option_new_" + optionOrder,
                    "Created with ID: " + savedNewOption.getId()
                );
            }

            optionOrder++;
        }

        for (final Map.Entry<Integer, Boolean> entry : processedOptions.entrySet()) {
            if (!entry.getValue()) {
                formQuestionOptionRepository.findById(entry.getKey()).ifPresent(option -> {
                    deleteOptionWithImage(option);
                    results.put("question_" + question.getId() + "_option_" + entry.getKey(), "Deleted");
                });
            }
        }

        return imageIndex;
    }

    private void updateOrCreateQuestionImage(FormQuestionModel question, File imageFile) {
        try {
            final byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            final ImageModel currentImage = question.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                final String altText = "Question image for: " + question.getCategory();
                final String url = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "form/questions",
                    imageFile.getName(),
                    altText
                );
                final String path = url.substring(url.indexOf("/images/") + 1);
                final ImageModel newImage = imageRepository.findByPath(path)
                    .orElseThrow(() -> HttpException.notFound("Image with path " + path + " not found"));
                question.setImage(newImage);
                formQuestionRepository.save(question);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing question image: " + e.getMessage(), e);
        }
    }

    private void updateOrCreateOptionImage(FormQuestionOptionModel option, File imageFile) {
        try {
            final byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            final ImageModel currentImage = option.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                final String altText = "Option image: " + (option.getText() != null ? option.getText() : "option");
                final String url = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "form/questions/options",
                    imageFile.getName(),
                    altText
                );
                final String path = url.substring(url.indexOf("/images/") + 1);
                final ImageModel newImage = imageRepository.findByPath(path)
                    .orElseThrow(() -> HttpException.notFound("Image with path " + path + " not found"));
                option.setImage(newImage);
                formQuestionOptionRepository.save(option);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing option image: " + e.getMessage(), e);
        }
    }

    private void deleteImageFromEntity(FormQuestionModel question) {
        final ImageModel oldImage = question.getImage();
        question.setImage(null);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteImageFromEntity(FormQuestionOptionModel option) {
        final ImageModel oldImage = option.getImage();
        option.setImage(null);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteQuestionWithImage(FormQuestionModel question) {
        final ImageModel image = question.getImage();
        formQuestionRepository.deleteById(question.getId());
        if (image != null) {
            imageService.deleteImage(image.getPath());
        }
    }

    private void deleteOptionWithImage(FormQuestionOptionModel option) {
        final ImageModel image = option.getImage();
        formQuestionOptionRepository.deleteById(option.getId());
        if (image != null) {
            imageService.deleteImage(image.getPath());
        }
    }

    private boolean checkChangeByQuestion(FormQuestionModel saved, FormQuestionUpdate patch) {
        return switch (patch) {
            case FormQuestionUpdate.SelectMultipleUpdate sm -> {
                yield !Objects.equals(saved.getCategory(), sm.category)
                      || !Objects.equals(saved.getText(), sm.text.orElse(null))
                      || saved.getMin() != sm.min
                      || saved.getMax() != sm.max
                      || saved.getOther() != sm.other;
            }

            case FormQuestionUpdate.SelectOneUpdate so -> {
                yield !Objects.equals(saved.getCategory(), so.category)
                      || !Objects.equals(saved.getText(), so.text.orElse(null))
                      || saved.getOther() != so.other;
            }

            case FormQuestionUpdate.SliderUpdate sl -> {
                yield !Objects.equals(saved.getCategory(), sl.category)
                      || !Objects.equals(saved.getText(), sl.text.orElse(null))
                      || !Objects.equals(saved.getPlaceholder(), sl.placeholder)
                      || saved.getMin() != sl.min
                      || saved.getMax() != sl.max
                      || saved.getStep() != sl.step;
            }

            case FormQuestionUpdate.TextLongUpdate tl -> {
                yield !Objects.equals(saved.getCategory(), tl.category)
                      || !Objects.equals(saved.getText(), tl.text.orElse(null))
                      || !Objects.equals(saved.getPlaceholder(), tl.placeholder)
                      || saved.getMinLength() != tl.minLength
                      || saved.getMaxLength() != tl.maxLength;
            }

            case FormQuestionUpdate.TextShortUpdate ts -> {
                yield !Objects.equals(saved.getCategory(), ts.category)
                      || !Objects.equals(saved.getText(), ts.text.orElse(null))
                      || !Objects.equals(saved.getPlaceholder(), ts.placeholder)
                      || saved.getMinLength() != ts.minLength
                      || saved.getMaxLength() != ts.maxLength;
            }
        };
    }

    private Map<String, String> processConfigGeneralData(ConfigModel savedConfig, ConfigUpdate updatedConfig) {
        final Map<String, String> updates = new HashMap<>();

        if (!Objects.equals(savedConfig.getTitle(), updatedConfig.title())) {
            savedConfig.setTitle(updatedConfig.title());
            updates.put("title", "The new title is: " + savedConfig.getTitle());
        }

        if (!Objects.equals(savedConfig.getSubtitle(), String.valueOf(updatedConfig.subtitle()))) {
            savedConfig.setSubtitle(String.valueOf(updatedConfig.subtitle()));
            updates.put("subtitle", "The new subtitle is: " + savedConfig.getSubtitle());
        }

        if (!Objects.equals(savedConfig.getDescription(), String.valueOf(updatedConfig.description()))) {
            savedConfig.setDescription(String.valueOf(updatedConfig.description()));
            updates.put("description", "The new description is: " + savedConfig.getDescription());
        }

        if (savedConfig.isAnonymous() != updatedConfig.anonymous()) {
            savedConfig.setAnonymous(updatedConfig.anonymous());
            updates.put(
                "anonymous",
                "The configuration now is" + (savedConfig.isAnonymous() ? "anonymous" : "not " + "anonymous")
            );
        }

        if (!Objects.equals(savedConfig.getInformedConsent(), updatedConfig.informedConsent())) {
            savedConfig.setInformedConsent(updatedConfig.informedConsent());
            updates.put("informedConsent", "The new informed consent is: " + savedConfig.getInformedConsent());
        }

        return updates;
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        final File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private Map<Integer, File> convertMultipartFileMap(Map<Integer, MultipartFile> multipartFiles) {
        final Map<Integer, File> result = new HashMap<>();

        for (final Map.Entry<Integer, MultipartFile> entry : multipartFiles.entrySet()) {
            try {
                final File file = convertMultipartFileToFile(entry.getValue());
                result.put(entry.getKey(), file);
            } catch (IOException e) {
                throw new RuntimeException("Error converting multipart file at index " + entry.getKey(), e);
            }
        }

        return result;
    }

    private static List<String> parseJsonStringArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return OBJECT_MAPPER.readValue(
                json, new TypeReference<>() {
                }
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse arguments JSON", e);
        }
    }
}
