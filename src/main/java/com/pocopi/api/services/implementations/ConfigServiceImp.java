package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Config.PatchLastConfig;
import com.pocopi.api.dto.Config.PatchRequest;
import com.pocopi.api.dto.Config.PatchResponse;
import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.Form.PatchForm;
import com.pocopi.api.dto.FormQuestion.FormQuestion;
import com.pocopi.api.dto.FormQuestion.PatchFormQuestion;
import com.pocopi.api.dto.FormQuestionOption.FormOption;
import com.pocopi.api.dto.FormQuestionOption.PatchFormOption;
import com.pocopi.api.dto.HomeFaq.Faq;
import com.pocopi.api.dto.HomeInfoCard.InformationCard;
import com.pocopi.api.dto.Image.Image;
import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.dto.SliderLabel.SliderLabel;
import com.pocopi.api.dto.TestGroup.*;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.HomeFaqModel;
import com.pocopi.api.models.config.HomeInfoCardModel;
import com.pocopi.api.models.config.TranslationModel;
import com.pocopi.api.models.form.*;
import com.pocopi.api.models.image.ImageModel;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImp implements ConfigService {
    private final ConfigRepository configRepository;
    private final TranslationRepository translationRepository;
    private final FormRepository formRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final HomeFaqService homeFaqService;
    private final HomeInfoCardService homeInfoCardService;
    private final ImageService imageService;
    private final TestGroupService testGroupService;

    public ConfigServiceImp(ConfigRepository configRepository,
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

    @Override
    public SingleConfigResponse getLastConfig() {
        ConfigModel configModel = findLastConfig();
        int configId = configModel.getVersion();

        Image icon = null;
        if (configModel.getIcon().getPath() != null) {
            icon = imageService.getImageByPath(configModel.getIcon().getPath());
        }

        List<TranslationModel> translations = translationRepository.findAllByConfigVersion(configId);
        List<HomeInfoCardModel> homeInfoCardModels = homeInfoCardService.findAllByConfigVersion(configId);
        List<HomeFaqModel> homeFaqs = homeFaqService.findAllByConfigVersion(configId);
        List<FormModel> forms = formRepository.findAllByConfigVersion(configId);
        Form preTest = null;
        Form postTest = null;

        if (forms.size() == 2) {
            FormModel firstForm = forms.get(0);
            FormModel secondForm = forms.get(1);

            List<FormProjection> firstRows = formRepository.findFormWithAllData(firstForm.getConfig().getVersion());
            List<FormProjection> secondRows = formRepository.findFormWithAllData(secondForm.getConfig().getVersion());

            if (firstForm.getType() == FormType.PRE) {
                preTest = generateFormFromQuery(firstRows);
                postTest = generateFormFromQuery(secondRows);
            } else {
                preTest = generateFormFromQuery(secondRows);
                postTest = generateFormFromQuery(firstRows);
            }
        }
        Map<String, String> translationMap = new HashMap<>();
        for (TranslationModel translation : translations) {
            translationMap.put(translation.getKey(), translation.getValue());
        }

        List<InformationCard> informationCards = new ArrayList<>();
        for (HomeInfoCardModel homeInfoCardModel : homeInfoCardModels) {
            Image iconByInfoCard = null;
            if (homeInfoCardModel.getIcon().getPath() != null) {
                iconByInfoCard = imageService.getImageByPath(homeInfoCardModel.getIcon().getPath());
            }
            InformationCard informationCard = new InformationCard(
                homeInfoCardModel.getId(),
                homeInfoCardModel.getTitle(),
                homeInfoCardModel.getDescription(),
                homeInfoCardModel.getColor(),
                Optional.ofNullable(iconByInfoCard)
            );
            informationCards.add(informationCard);
        }
        List<Faq> faqs = new ArrayList<>();
        for (HomeFaqModel faq : homeFaqs) {
            faqs.add(new Faq(faq.getId(),faq.getQuestion(), faq.getAnswer()));
        }
        Map<String, Group> groups = testGroupService.buildGroupResponses(configId);

        return new SingleConfigResponse(
            configId,
            Optional.ofNullable(icon),
            configModel.getTitle(),
            Optional.ofNullable(configModel.getSubtitle()),
            configModel.getDescription(),
            configModel.isAnonymous(),
            informationCards,
            configModel.getInformedConsent(),
            faqs,
            Optional.ofNullable(preTest),
            Optional.ofNullable(postTest),
            groups,
            translationMap
        );
    }

    @Override
    public ConfigModel findLastConfig() {
        return configRepository.findLastConfig();
    }

    @Override
    public PatchResponse processUpdatedConfig(PatchRequest request) {
        ConfigModel savedModel = configRepository.getByVersion(request.updateLastConfig().version());

        Map<Integer, File> preTestFiles = convertMultipartFileMap(request.preTestFormQuestionOptionsFiles());
        Map<Integer, File> postTestFiles = convertMultipartFileMap(request.postTestFormQuestionOptionsFiles());
        Map<Integer, File> groupFiles = convertMultipartFileMap(request.groupQuestionOptionsFiles());
        Map<Integer, File> infoCardFiles = convertMultipartFileMap(request.informationCardFiles());

        Map<String, String> configUpdatesSummary = processConfigGeneralData(savedModel, request.updateLastConfig());
        Map<String, String> informationCardUpdatesSummary = homeInfoCardService.processCardInformation(
            request.updateLastConfig().informationCards(),
            infoCardFiles
        );
        Map<String, String> faqUpdatedSummary = homeFaqService.processFaq(request.updateLastConfig().faq());
        Map<String, String> preTestUpdatedSummary = processFormQuestions(request.updateLastConfig().preTestForm(), preTestFiles);
        Map<String, String> postTestUpdatedSummary = processFormQuestions(request.updateLastConfig().postTestForm(), postTestFiles);
        Map<String, String> groupSummary = testGroupService.processGroups(request.updateLastConfig().groups(), groupFiles);

        return new PatchResponse(
            configUpdatesSummary,
            informationCardUpdatesSummary,
            faqUpdatedSummary,
            preTestUpdatedSummary,
            postTestUpdatedSummary,
            groupSummary
        );
    }

    private Map<String, String> processFormQuestions(PatchForm updatedForm, Map<Integer, File> images) {
        Map<String, String> results = new HashMap<>();
        List<FormQuestionModel> allExistingQuestions = formQuestionRepository.findAll();
        Map<Integer, Boolean> processedQuestions = new HashMap<>();

        for (FormQuestionModel question : allExistingQuestions) {
            processedQuestions.put(question.getId(), false);
        }

        int order = 0;
        int imageIndex = 0;

        for (PatchFormQuestion patchQuestion : updatedForm.questions()) {
            switch (patchQuestion) {
                case PatchFormQuestion.PatchSelectMultiple selectMultiple -> {
                    if (selectMultiple.id.isPresent()) {
                        Integer questionId = selectMultiple.id.get();
                        FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, selectMultiple);
                        boolean orderChanged = savedQuestion.getOrder() != order;

                        boolean hasImageChange = questionImage != null;
                        boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        boolean replaceImage = hasImageChange && questionImage.length() > 0;

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

                        imageIndex = processQuestionOptions(savedQuestion, selectMultiple.options, images, imageIndex, results);
                        processedQuestions.put(questionId, true);

                    } else {
                        // Nueva pregunta
                        FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(selectMultiple.category);
                        newQuestion.setText(selectMultiple.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setMax((short) selectMultiple.max);
                        newQuestion.setMin((short) selectMultiple.min);
                        newQuestion.setType(FormQuestionType.SELECT_MULTIPLE);
                        newQuestion.setOther(selectMultiple.other);

                        FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        imageIndex = processQuestionOptions(savedNewQuestion, selectMultiple.options, images, imageIndex, results);
                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case PatchFormQuestion.PatchSelectOne selectOne -> {
                    if (selectOne.id.isPresent()) {
                        Integer questionId = selectOne.id.get();
                        FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, selectOne);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean hasImageChange = questionImage != null;
                        boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        boolean replaceImage = hasImageChange && questionImage.length() > 0;

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

                        imageIndex = processQuestionOptions(savedQuestion, selectOne.options, images, imageIndex, results);
                        processedQuestions.put(questionId, true);

                    } else {
                        FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(selectOne.category);
                        newQuestion.setText(selectOne.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.SELECT_ONE);
                        newQuestion.setOther(selectOne.other);

                        FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        imageIndex = processQuestionOptions(savedNewQuestion, selectOne.options, images, imageIndex, results);
                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case PatchFormQuestion.PatchSlider slider -> {
                    if (slider.id.isPresent()) {
                        Integer questionId = slider.id.get();
                        FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, slider);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean hasImageChange = questionImage != null;
                        boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        boolean replaceImage = hasImageChange && questionImage.length() > 0;

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
                        FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(slider.category);
                        newQuestion.setText(slider.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.SLIDER);
                        newQuestion.setPlaceholder(slider.placeholder);
                        newQuestion.setMin((short) slider.min);
                        newQuestion.setMax((short) slider.max);
                        newQuestion.setStep((short) slider.step);

                        FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case PatchFormQuestion.PatchTextLong textLong -> {
                    if (textLong.id.isPresent()) {
                        Integer questionId = textLong.id.get();
                        FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, textLong);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean hasImageChange = questionImage != null;
                        boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        boolean replaceImage = hasImageChange && questionImage.length() > 0;

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
                        FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(textLong.category);
                        newQuestion.setText(textLong.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.TEXT_LONG);
                        newQuestion.setPlaceholder(textLong.placeholder);
                        newQuestion.setMinLength((short) textLong.minLength);
                        newQuestion.setMaxLength((short) textLong.maxLength);

                        FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        if (questionImage != null && questionImage.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImage);
                        }

                        results.put("question_new_" + order, "Created with ID: " + savedNewQuestion.getId());
                    }
                }

                case PatchFormQuestion.PatchTextShort textShort -> {
                    if (textShort.id.isPresent()) {
                        Integer questionId = textShort.id.get();
                        FormQuestionModel savedQuestion = formQuestionRepository.getFormQuestionModelById(questionId);

                        if (savedQuestion == null) {
                            results.put("question_" + questionId, "Question not found");
                            order++;
                            imageIndex++;
                            continue;
                        }

                        File questionImage = images.get(imageIndex);
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, textShort);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean hasImageChange = questionImage != null;
                        boolean deleteImage = hasImageChange && questionImage.length() == 0;
                        boolean replaceImage = hasImageChange && questionImage.length() > 0;

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
                        FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(textShort.category);
                        newQuestion.setText(textShort.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setType(FormQuestionType.TEXT_SHORT);
                        newQuestion.setPlaceholder(textShort.placeholder);
                        newQuestion.setMinLength((short) textShort.minLength);
                        newQuestion.setMaxLength((short) textShort.maxLength);

                        FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        File questionImage = images.get(imageIndex);
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

        for (Map.Entry<Integer, Boolean> entry : processedQuestions.entrySet()) {
            if (!entry.getValue()) {
                FormQuestionModel questionToDelete = formQuestionRepository.findById(entry.getKey()).orElse(null);
                if (questionToDelete != null) {
                    deleteQuestionWithImage(questionToDelete);
                    results.put("question_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private int processQuestionOptions(FormQuestionModel question,
                                       List<PatchFormOption> patchOptions,
                                       Map<Integer, File> images,
                                       int currentImageIndex,
                                       Map<String, String> results) {
        List<FormQuestionOptionModel> allExistingOptions = formQuestionOptionRepository.findAllByFormQuestion_Id(question.getId());
        Map<Integer, Boolean> processedOptions = new HashMap<>();

        for (FormQuestionOptionModel option : allExistingOptions) {
            processedOptions.put(option.getId(), false);
        }

        int optionOrder = 0;
        int imageIndex = currentImageIndex;

        for (PatchFormOption patchOption : patchOptions) {
            if (patchOption.id().isPresent()) {
                Integer optionId = patchOption.id().get();
                FormQuestionOptionModel savedOption = formQuestionOptionRepository.getFormQuestionOptionModelById(optionId);

                if (savedOption == null) {
                    results.put("question_" + question.getId() + "_option_" + optionId, "Option not found");
                    optionOrder++;
                    imageIndex++;
                    continue;
                }

                File optionImage = images.get(imageIndex);
                imageIndex++;

                boolean textChanged = !Objects.equals(savedOption.getText(), patchOption.text().orElse(null));
                boolean orderChanged = savedOption.getOrder() != optionOrder;
                boolean hasImageChange = optionImage != null;
                boolean deleteImage = hasImageChange && optionImage.length() == 0;
                boolean replaceImage = hasImageChange && optionImage.length() > 0;

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
                FormQuestionOptionModel newOption = new FormQuestionOptionModel();
                newOption.setText(patchOption.text().orElse(null));
                newOption.setOrder((byte) optionOrder);
                newOption.setFormQuestion(question);

                FormQuestionOptionModel savedNewOption = formQuestionOptionRepository.save(newOption);

                File optionImage = images.get(imageIndex);
                imageIndex++;

                if (optionImage != null && optionImage.length() > 0) {
                    updateOrCreateOptionImage(savedNewOption, optionImage);
                }

                results.put("question_" + question.getId() + "_option_new_" + optionOrder, "Created with ID: " + savedNewOption.getId());
            }

            optionOrder++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedOptions.entrySet()) {
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
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            ImageModel currentImage = question.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                String altText = "Question image for: " + question.getCategory();
                UploadImageResponse response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "form/questions",
                    imageFile.getName(),
                    altText
                );
                String path = response.url().substring(response.url().indexOf("/images/") + 1);
                ImageModel newImage = imageService.getImageModelByPath(path);
                question.setImage(newImage);
                formQuestionRepository.save(question);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing question image: " + e.getMessage(), e);
        }
    }

    private void updateOrCreateOptionImage(FormQuestionOptionModel option, File imageFile) {
        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            ImageModel currentImage = option.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                String altText = "Option image: " + (option.getText() != null ? option.getText() : "option");
                UploadImageResponse response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "form/questions/options",
                    imageFile.getName(),
                    altText
                );
                String path = response.url().substring(response.url().indexOf("/images/") + 1);
                ImageModel newImage = imageService.getImageModelByPath(path);
                option.setImage(newImage);
                formQuestionOptionRepository.save(option);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing option image: " + e.getMessage(), e);
        }
    }

    private void deleteImageFromEntity(FormQuestionModel question) {
        ImageModel oldImage = question.getImage();
        question.setImage(null);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteImageFromEntity(FormQuestionOptionModel option) {
        ImageModel oldImage = option.getImage();
        option.setImage(null);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteQuestionWithImage(FormQuestionModel question) {
        ImageModel image = question.getImage();
        formQuestionRepository.deleteById(question.getId());
        if (image != null) {
            imageService.deleteImage(image.getPath());
        }
    }

    private void deleteOptionWithImage(FormQuestionOptionModel option) {
        ImageModel image = option.getImage();
        formQuestionOptionRepository.deleteById(option.getId());
        if (image != null) {
            imageService.deleteImage(image.getPath());
        }
    }

    private boolean checkChangeByQuestion(FormQuestionModel saved, PatchFormQuestion patch) {
        return switch (patch) {
            case PatchFormQuestion.PatchSelectMultiple sm ->
                !Objects.equals(saved.getCategory(), sm.category) ||
                    !Objects.equals(saved.getText(), sm.text.orElse(null)) ||
                    saved.getMin() != sm.min ||
                    saved.getMax() != sm.max ||
                    saved.getOther() != sm.other;

            case PatchFormQuestion.PatchSelectOne so ->
                !Objects.equals(saved.getCategory(), so.category) ||
                    !Objects.equals(saved.getText(), so.text.orElse(null)) ||
                    saved.getOther() != so.other;

            case PatchFormQuestion.PatchSlider sl ->
                !Objects.equals(saved.getCategory(), sl.category) ||
                    !Objects.equals(saved.getText(), sl.text.orElse(null)) ||
                    !Objects.equals(saved.getPlaceholder(), sl.placeholder) ||
                    saved.getMin() != sl.min ||
                    saved.getMax() != sl.max ||
                    saved.getStep() != sl.step;

            case PatchFormQuestion.PatchTextLong tl ->
                !Objects.equals(saved.getCategory(), tl.category) ||
                    !Objects.equals(saved.getText(), tl.text.orElse(null)) ||
                    !Objects.equals(saved.getPlaceholder(), tl.placeholder) ||
                    saved.getMinLength() != tl.minLength ||
                    saved.getMaxLength() != tl.maxLength;

            case PatchFormQuestion.PatchTextShort ts ->
                !Objects.equals(saved.getCategory(), ts.category) ||
                    !Objects.equals(saved.getText(), ts.text.orElse(null)) ||
                    !Objects.equals(saved.getPlaceholder(), ts.placeholder) ||
                    saved.getMinLength() != ts.minLength ||
                    saved.getMaxLength() != ts.maxLength;
        };
    }

    private Map<String, String> processConfigGeneralData(ConfigModel savedConfig, PatchLastConfig updatedConfig) {
        Map<String, String> updates = new HashMap<>();

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
            updates.put("anonymous", "The configuration now is" + (savedConfig.isAnonymous() ? "anonymous" : "not " +
                "anonymous"));
        }

        if (!Objects.equals(savedConfig.getInformedConsent(), updatedConfig.informedConsent())) {
            savedConfig.setInformedConsent(updatedConfig.informedConsent());
            updates.put("informedConsent", "The new informed consent is: " + savedConfig.getInformedConsent());
        }

        return updates;
    }


    private Form generateFormFromQuery(List<FormProjection> rows) {
        Map<Integer, List<FormProjection>> groupedByQuestion = rows.stream()
            .collect(Collectors.groupingBy(FormProjection::getQuestionId));

        List<FormQuestion> questions = new ArrayList<>();

        for (Map.Entry<Integer, List<FormProjection>> entry : groupedByQuestion.entrySet()) {
            List<FormProjection> group = entry.getValue();
            FormProjection first = group.getFirst();

            Image imageResponse = null;
            if (first.getQuestionImagePath() != null) {
                imageResponse = imageService.getImageByPath(first.getQuestionImagePath());
            }

            FormQuestionType type = first.getQuestionType();
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
                    List<SliderLabel> labels = group.stream()
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
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private Map<Integer, File> convertMultipartFileMap(Map<Integer, MultipartFile> multipartFiles) {
        Map<Integer, File> result = new HashMap<>();

        for (Map.Entry<Integer, MultipartFile> entry : multipartFiles.entrySet()) {
            try {
                File file = convertMultipartFileToFile(entry.getValue());
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
