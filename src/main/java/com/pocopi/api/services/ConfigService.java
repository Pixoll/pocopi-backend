package com.pocopi.api.services;

import com.pocopi.api.dto.config.Config;
import com.pocopi.api.dto.config.ConfigUpdate;
import com.pocopi.api.dto.config.ConfigUpdateWithFiles;
import com.pocopi.api.dto.config.UpdatedConfig;
import com.pocopi.api.dto.form.Form;
import com.pocopi.api.dto.form.FormUpdate;
import com.pocopi.api.dto.form_question.FormQuestion;
import com.pocopi.api.dto.form_question.FormQuestionUpdate;
import com.pocopi.api.dto.form_question.SliderLabel;
import com.pocopi.api.dto.form_question_option.FormOption;
import com.pocopi.api.dto.form_question_option.FormOptionUpdate;
import com.pocopi.api.dto.home_faq.FrequentlyAskedQuestion;
import com.pocopi.api.dto.home_info_card.InformationCard;
import com.pocopi.api.dto.image.Image;
import com.pocopi.api.dto.image.ImageUrl;
import com.pocopi.api.dto.test.TestGroup;
import com.pocopi.api.dto.translation.Translation;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.image.ImageModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.repositories.projections.FormProjection;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigService {
    private final ConfigRepository configRepository;
    private final TranslationRepository translationRepository;
    private final FormRepository formRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final HomeFaqService homeFaqService;
    private final HomeInfoCardService homeInfoCardService;
    private final ImageService imageService;
    private final TestGroupService testGroupService;

    public ConfigService(
        ConfigRepository configRepository,
        TranslationRepository translationRepository,
        FormRepository formRepository,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository,
        HomeFaqService homeFaqService,
        HomeInfoCardService homeInfoCardService,
        ImageService imageService,
        TestGroupService testGroupService
    ) {
        this.configRepository = configRepository;
        this.translationRepository = translationRepository;
        this.formRepository = formRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.homeFaqService = homeFaqService;
        this.homeInfoCardService = homeInfoCardService;
        this.imageService = imageService;
        this.testGroupService = testGroupService;
    }

    public Config getLastConfig() {
        final ConfigModel configModel = findLastConfig();
        final int configId = configModel.getVersion();

        Image icon = null;
        if (configModel.getIcon().getPath() != null) {
            icon = imageService.getImageByPath(configModel.getIcon().getPath());
        }

        final List<Translation> translations = translationRepository.findAllByConfigVersion(configId);
        final List<HomeInfoCardModel> homeInfoCardModels = homeInfoCardService.findAllByConfigVersion(configId);
        final List<HomeFaqModel> homeFaqs = homeFaqService.findAllByConfigVersion(configId);
        final List<FormModel> forms = formRepository.findAllByConfigVersion(configId);
        Form preTest = null;
        Form postTest = null;

        if (forms.size() == 2) {
            final FormModel firstForm = forms.get(0);
            final FormModel secondForm = forms.get(1);

            final List<FormProjection> firstRows = formRepository.findFormWithAllData(firstForm.getConfig()
                .getVersion());
            final List<FormProjection> secondRows = formRepository.findFormWithAllData(secondForm.getConfig()
                .getVersion());

            if (firstForm.getType() == FormType.PRE) {
                preTest = generateFormFromQuery(firstRows);
                postTest = generateFormFromQuery(secondRows);
            } else {
                preTest = generateFormFromQuery(secondRows);
                postTest = generateFormFromQuery(firstRows);
            }
        }
        final Map<String, String> translationMap = new HashMap<>();
        for (final Translation translation : translations) {
            translationMap.put(translation.key(), translation.value());
        }

        final List<InformationCard> informationCards = new ArrayList<>();
        for (final HomeInfoCardModel homeInfoCardModel : homeInfoCardModels) {
            Image iconByInfoCard = null;
            if (homeInfoCardModel.getIcon().getPath() != null) {
                iconByInfoCard = imageService.getImageByPath(homeInfoCardModel.getIcon().getPath());
            }
            final InformationCard informationCard = new InformationCard(
                homeInfoCardModel.getId(),
                homeInfoCardModel.getTitle(),
                homeInfoCardModel.getDescription(),
                homeInfoCardModel.getColor(),
                Optional.ofNullable(iconByInfoCard)
            );
            informationCards.add(informationCard);
        }
        final List<FrequentlyAskedQuestion> frequentlyAskedQuestions = new ArrayList<>();
        for (final HomeFaqModel faq : homeFaqs) {
            frequentlyAskedQuestions.add(new FrequentlyAskedQuestion(faq.getId(), faq.getQuestion(), faq.getAnswer()));
        }
        final Map<String, TestGroup> groups = testGroupService.buildGroupResponses(configId);

        return new Config(
            configId,
            Optional.ofNullable(icon),
            configModel.getTitle(),
            Optional.ofNullable(configModel.getSubtitle()),
            configModel.getDescription(),
            configModel.isAnonymous(),
            informationCards,
            configModel.getInformedConsent(),
            frequentlyAskedQuestions,
            Optional.ofNullable(preTest),
            Optional.ofNullable(postTest),
            groups,
            translationMap
        );
    }

    public ConfigModel findLastConfig() {
        return configRepository.findLastConfig();
    }

    public UpdatedConfig processUpdatedConfig(ConfigUpdateWithFiles request) {
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
                final ImageUrl response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "form/questions",
                    imageFile.getName(),
                    altText
                );
                final String path = response.url().substring(response.url().indexOf("/images/") + 1);
                final ImageModel newImage = imageService.getImageModelByPath(path);
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
                final ImageUrl response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "form/questions/options",
                    imageFile.getName(),
                    altText
                );
                final String path = response.url().substring(response.url().indexOf("/images/") + 1);
                final ImageModel newImage = imageService.getImageModelByPath(path);
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

    private Form generateFormFromQuery(List<FormProjection> rows) {
        final Map<Integer, List<FormProjection>> groupedByQuestion = rows.stream()
            .collect(Collectors.groupingBy(FormProjection::getQuestionId));

        final List<FormQuestion> questions = new ArrayList<>();

        for (final Map.Entry<Integer, List<FormProjection>> entry : groupedByQuestion.entrySet()) {
            final List<FormProjection> group = entry.getValue();
            final FormProjection first = group.getFirst();

            Image imageResponse = null;
            if (first.getQuestionImagePath() != null) {
                imageResponse = imageService.getImageByPath(first.getQuestionImagePath());
            }

            final FormQuestionType type = first.getQuestionType();
            switch (type) {
                case SELECT_MULTIPLE -> questions.add(new FormQuestion.SelectMultiple(
                    first.getQuestionId(),
                    first.getCategory(),
                    Optional.ofNullable(first.getQuestionText()),
                    imageResponse != null ? Optional.of(imageResponse) : Optional.empty(),
                    type,
                    this.getOptionsFromGroup(group),
                    first.getMin(),
                    first.getMax(),
                    first.getOther()
                ));

                case SELECT_ONE -> questions.add(new FormQuestion.SelectOne(
                    first.getQuestionId(),
                    first.getCategory(),
                    Optional.ofNullable(first.getQuestionText()),
                    imageResponse != null ? Optional.of(imageResponse) : Optional.empty(),
                    type,
                    this.getOptionsFromGroup(group),
                    first.getOther()
                ));

                case SLIDER -> {
                    final List<SliderLabel> labels = group.stream()
                        .filter(r -> r.getSliderValue() != null)
                        .map(r -> new SliderLabel(r.getSliderValue(), r.getSliderLabel()))
                        .toList();

                    questions.add(new FormQuestion.Slider(
                        first.getQuestionId(),
                        first.getCategory(),
                        Optional.ofNullable(first.getQuestionText()),
                        imageResponse != null ? Optional.of(imageResponse) : Optional.empty(),
                        type,
                        first.getPlaceholder(),
                        first.getMin(),
                        first.getMax(),
                        first.getStep(),
                        labels
                    ));
                }

                case TEXT_SHORT -> questions.add(new FormQuestion.TextShort(
                    first.getQuestionId(),
                    first.getCategory(),
                    Optional.ofNullable(first.getQuestionText()),
                    imageResponse != null ? Optional.of(imageResponse) : Optional.empty(),
                    type,
                    first.getPlaceholder(),
                    first.getMinLength(),
                    first.getMaxLength()
                ));
                case TEXT_LONG -> questions.add(new FormQuestion.TextLong(
                    first.getQuestionId(),
                    first.getCategory(),
                    Optional.ofNullable(first.getQuestionText()),
                    imageResponse != null ? Optional.of(imageResponse) : Optional.empty(),
                    type,
                    first.getPlaceholder(),
                    first.getMinLength(),
                    first.getMaxLength()
                ));
            }
        }

        return new Form(rows.getFirst().getFormId(), questions);
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

    private List<FormOption> getOptionsFromGroup(List<FormProjection> group) {
        return group.stream()
            .filter(r -> r.getOptionId() != null)
            .map(r -> {
                Image imageResponseBySMOptions = null;
                if (r.getOptionImagePath() != null) {
                    imageResponseBySMOptions = imageService.getImageByPath(r.getOptionImagePath());
                }
                return new FormOption(
                    r.getOptionId(),
                    Optional.ofNullable(r.getOptionText()),
                    imageResponseBySMOptions != null ? Optional.of(imageResponseBySMOptions) :
                        Optional.empty()
                );
            })
            .toList();
    }
}
