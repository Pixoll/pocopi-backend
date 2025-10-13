package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Config.PatchLastConfig;
import com.pocopi.api.dto.Config.PatchRequest;
import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.Form.PatchForm;
import com.pocopi.api.dto.FormQuestion.FormQuestion;
import com.pocopi.api.dto.FormQuestion.PatchFormQuestion;
import com.pocopi.api.dto.FormQuestionOption.FormOption;
import com.pocopi.api.dto.FormQuestionOption.PatchFormOption;
import com.pocopi.api.dto.HomeFaq.Faq;
import com.pocopi.api.dto.HomeFaq.PatchFaq;
import com.pocopi.api.dto.HomeInfoCard.InformationCard;
import com.pocopi.api.dto.HomeInfoCard.PatchInformationCard;
import com.pocopi.api.dto.Image.Image;
import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.dto.SliderLabel.SliderLabel;
import com.pocopi.api.dto.TestGroup.*;
import com.pocopi.api.models.*;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.interfaces.ConfigService;
import com.pocopi.api.services.interfaces.ImageService;
import com.pocopi.api.services.interfaces.TestGroupService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImp implements ConfigService {
    private final ConfigRepository configRepository;
    private final TranslationRepository translationRepository;
    private final HomeInfoCardRepository homeInfoCardRepository;
    private final HomeFaqRepository homeFaqRepository;
    private final FormRepository formRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final TestProtocolRepository  testProtocolRepository;
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final ImageService imageService;
    private final TestGroupService testGroupService;

    public ConfigServiceImp(ConfigRepository configRepository,
                            TranslationRepository translationRepository,
                            HomeInfoCardRepository homeInfoCardRepository,
                            HomeFaqRepository homeFaqRepository,
                            FormRepository formRepository,
                            FormQuestionRepository formQuestionRepository,
                            FormQuestionOptionRepository formQuestionOptionRepository,
                            TestProtocolRepository  testProtocolRepository,
                            TestPhaseRepository  testPhaseRepository,
                            TestQuestionRepository  testQuestionRepository,
                            TestOptionRepository  testOptionRepository,
                            ImageService imageService,
                            TestGroupService testGroupService
    ) {
        this.configRepository = configRepository;
        this.translationRepository = translationRepository;
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.homeFaqRepository = homeFaqRepository;
        this.formRepository = formRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.testProtocolRepository = testProtocolRepository;
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
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
        List<HomeInfoCardModel> homeInfoCardModels = homeInfoCardRepository.findAllByConfigVersion(configId);
        List<HomeFaqModel> homeFaqs = homeFaqRepository.findAllByConfigVersion(configId);
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
            InformationCard informationCard = new InformationCard(homeInfoCardModel.getTitle(),
                homeInfoCardModel.getDescription(),
                homeInfoCardModel.getColor(),
                Optional.ofNullable(iconByInfoCard)
            );
            informationCards.add(informationCard);
        }
        List<Faq> faqs = new ArrayList<>();
        for (HomeFaqModel faq : homeFaqs) {
            faqs.add(new Faq(faq.getQuestion(), faq.getAnswer()));
        }
        Map<String, Group> groups = testGroupService.buildGroupResponses(configId);

        return new SingleConfigResponse(
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
    public String processUpdatedConfig(PatchRequest request) {
        ConfigModel savedModel = configRepository.getByVersion(request.updateLastConfig().version());

        if (savedModel == null) {
            return "Config not found";
        }

        Map<String, String> configUpdatesSummary = processConfigGeneralData(savedModel, request.updateLastConfig());
        Map<String, String> informationCardUpdatesSummary = processCardInformation(request.updateLastConfig().informationCards(), request.informationCardFiles());
        Map<String, String> faqUpdatedSummary = processFaq(request.updateLastConfig().faq());

        Map<String, String> preTestUpdatedSummary = processFormQuestions(request.updateLastConfig().preTestForm(), request.preTestFormQuestionOptionsFiles());
        Map<String, String> postTestUpdatedSummary = processFormQuestions(request.updateLastConfig().postTestForm(), request.preTestFormQuestionOptionsFiles());

        Map<String, String> groupSummary = processGroups(request.updateLastConfig().groups(), request.groupQuestionOptionsFiles());


        return "xd";

    }
    private Map<String, String> processGroups(Map<String, PatchGroup> groups, List<File> images) {
        Map<String, String> results = new HashMap<>();
        List<TestGroupModel> allExistingGroups = testGroupService.finAllTestGroups();
        Map<Integer, Boolean> processedGroups = new HashMap<>();

        for (TestGroupModel group : allExistingGroups) {
            processedGroups.put(group.getId(), false);
        }

        int imageIndex = 0;

        for (Map.Entry<String, PatchGroup> entry : groups.entrySet()) {
            String groupKey = entry.getKey();
            PatchGroup updatedGroup = entry.getValue();

            if (updatedGroup.id().isPresent()) {
                int groupId = updatedGroup.id().get();
                TestGroupModel savedGroup = testGroupService.getTestGroup(groupId);

                if (savedGroup == null) {
                    results.put("group_" + groupId, "Group not found");
                    continue;
                }

                boolean infoChanged = checkChangeByGroup(updatedGroup, savedGroup);

                if (infoChanged) {
                    savedGroup.setGreeting(updatedGroup.greeting());
                    savedGroup.setLabel(updatedGroup.label());
                    savedGroup.setProbability((byte) updatedGroup.probability());

                    testGroupService.saveTestGroup(savedGroup);
                }

                Map<String, String> protocolResults = processProtocol(
                    savedGroup,
                    updatedGroup.protocol(),
                    images,
                    imageIndex,
                    results
                );
                results.putAll(protocolResults);

                if (infoChanged) {
                    results.put("group_" + groupId, "Updated successfully");
                } else {
                    results.put("group_" + groupId, "No changes");
                }

                processedGroups.put(groupId, true);

            } else {
                TestGroupModel newGroup = new TestGroupModel();
                newGroup.setProbability((byte) updatedGroup.probability());
                newGroup.setGreeting(updatedGroup.greeting());
                newGroup.setLabel(updatedGroup.label());

                TestGroupModel savedGroup = testGroupService.saveTestGroup(newGroup);

                Map<String, String> protocolResults = processProtocol(
                    savedGroup,
                    updatedGroup.protocol(),
                    images,
                    imageIndex,
                    results
                );
                results.putAll(protocolResults);

                results.put("group_new_" + groupKey, "Created with ID: " + savedGroup.getId());
            }
        }

        for (Map.Entry<Integer, Boolean> entry : processedGroups.entrySet()) {
            if (!entry.getValue()) {
                TestGroupModel groupToDelete = testGroupService.getTestGroup(entry.getKey());
                if (groupToDelete != null) {
                    deleteGroupWithProtocol(groupToDelete);
                    results.put("group_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private Map<String, String> processProtocol(
        TestGroupModel group,
        PatchProtocol updatedProtocol,
        List<File> images,
        int imageIndex,
        Map<String, String> results
    ) {
        Map<String, String> protocolResults = new HashMap<>();

        if (updatedProtocol.id().isPresent()) {
            int protocolId = updatedProtocol.id().get();
            TestProtocolModel savedProtocol = testProtocolRepository.getById(protocolId);

            if (savedProtocol == null) {
                protocolResults.put("protocol_" + protocolId, "Protocol not found");
                return protocolResults;
            }

            boolean isChanged = checkProtocolChanged(updatedProtocol, savedProtocol);

            if (isChanged) {
                savedProtocol.setLabel(updatedProtocol.label());
                savedProtocol.setAllowPreviousPhase(updatedProtocol.allowPreviousPhase());
                savedProtocol.setAllowPreviousQuestion(updatedProtocol.allowPreviousQuestion());
                savedProtocol.setAllowSkipQuestion(updatedProtocol.allowSkipQuestion());
                testProtocolRepository.save(savedProtocol);
            }

            Map<String, String> phaseResults = processPhases(
                savedProtocol,
                updatedProtocol.phases(),
                images,
                imageIndex
            );
            protocolResults.putAll(phaseResults);

            if (isChanged) {
                protocolResults.put("protocol_" + protocolId, "Updated successfully");
            } else {
                protocolResults.put("protocol_" + protocolId, "No changes");
            }

        } else {
            TestProtocolModel newProtocol = new TestProtocolModel();
            newProtocol.setAllowPreviousPhase(updatedProtocol.allowPreviousPhase());
            newProtocol.setAllowPreviousQuestion(updatedProtocol.allowPreviousQuestion());
            newProtocol.setAllowSkipQuestion(updatedProtocol.allowSkipQuestion());
            newProtocol.setLabel(updatedProtocol.label());
            newProtocol.setGroup(group);

            TestProtocolModel savedProtocol = testProtocolRepository.save(newProtocol);

            Map<String, String> phaseResults = processPhases(
                savedProtocol,
                updatedProtocol.phases(),
                images,
                imageIndex
            );
            protocolResults.putAll(phaseResults);

            protocolResults.put("protocol_new", "Created with ID: " + savedProtocol.getId());
        }

        return protocolResults;
    }

    private Map<String, String> processPhases(
        TestProtocolModel protocol,
        List<PatchPhase> phases,
        List<File> images,
        int imageIndex
    ) {
        Map<String, String> results = new HashMap<>();
        List<TestPhaseModel> allExistingPhases = testPhaseRepository.findAllByProtocol((protocol));
        Map<Integer, Boolean> processedPhases = new HashMap<>();

        for (TestPhaseModel phase : allExistingPhases) {
            processedPhases.put(phase.getId(), false);
        }

        int order = 0;
        for (PatchPhase patchPhase : phases) {
            if (patchPhase.id().isPresent()) {
                int phaseId = patchPhase.id().get();
                TestPhaseModel savedPhase = testPhaseRepository.findById(phaseId).orElse(null);

                if (savedPhase == null) {
                    results.put("phase_" + phaseId, "Phase not found");
                    order++;
                    continue;
                }

                boolean orderChanged = savedPhase.getOrder() != order;

                if (orderChanged) {
                    savedPhase.setOrder((byte) order);
                    testPhaseRepository.save(savedPhase);
                }

                Map<String, String> questionResults = processTestQuestions(
                    savedPhase,
                    patchPhase.questions(),
                    images,
                    imageIndex
                );
                results.putAll(questionResults);

                if (orderChanged) {
                    results.put("phase_" + phaseId, "Updated successfully");
                } else {
                    results.put("phase_" + phaseId, "No changes");
                }

                processedPhases.put(phaseId, true);

            } else {
                TestPhaseModel newPhase = new TestPhaseModel();
                newPhase.setOrder((byte) order);
                newPhase.setProtocol(protocol);

                TestPhaseModel savedPhase = testPhaseRepository.save(newPhase);

                Map<String, String> questionResults = processTestQuestions(
                    savedPhase,
                    patchPhase.questions(),
                    images,
                    imageIndex
                );
                results.putAll(questionResults);

                results.put("phase_new_" + order, "Created with ID: " + savedPhase.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedPhases.entrySet()) {
            if (!entry.getValue()) {
                TestPhaseModel phaseToDelete = testPhaseRepository.findById(entry.getKey()).orElse(null);
                if (phaseToDelete != null) {
                    deletePhaseWithQuestions(phaseToDelete);
                    results.put("phase_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private Map<String, String> processTestQuestions(
        TestPhaseModel phase,
        List<PatchQuestion> questions,
        List<File> images,
        int imageIndex
    ) {
        Map<String, String> results = new HashMap<>();
        List<TestQuestionModel> allExistingQuestions = testQuestionRepository.findAllByPhase(phase);
        Map<Integer, Boolean> processedQuestions = new HashMap<>();

        for (TestQuestionModel question : allExistingQuestions) {
            processedQuestions.put(question.getId(), false);
        }

        int order = 0;
        for (PatchQuestion patchQuestion : questions) {
            if (patchQuestion.id().isPresent()) {
                int questionId = patchQuestion.id().get();
                TestQuestionModel savedQuestion = testQuestionRepository.findById(questionId).orElse(null);

                if (savedQuestion == null) {
                    results.put("question_" + questionId, "Question not found");
                    order++;
                    imageIndex++;
                    continue;
                }

                File questionImageFile = images != null && imageIndex < images.size()
                    ? images.get(imageIndex)
                    : null;
                imageIndex++;

                boolean textChanged = !Objects.equals(savedQuestion.getText(), patchQuestion.text());
                boolean orderChanged = savedQuestion.getOrder() != order;
                boolean deleteImage = questionImageFile != null && questionImageFile.length() == 0;
                boolean replaceImage = questionImageFile != null && questionImageFile.length() > 0;

                if (textChanged || orderChanged || deleteImage || replaceImage) {
                    savedQuestion.setText(patchQuestion.text());
                    savedQuestion.setOrder((byte) order);

                    if (deleteImage) {
                        deleteImageFromQuestion(savedQuestion);
                    } else if (replaceImage) {
                        updateOrCreateQuestionImage(savedQuestion, questionImageFile);
                    }

                    testQuestionRepository.save(savedQuestion);
                    results.put("question_" + questionId, "Updated successfully");
                } else {
                    results.put("question_" + questionId, "No changes");
                }

                Map<String, String> optionResults = processTestQuestionOptions(
                    savedQuestion,
                    patchQuestion.options(),
                    images,
                    imageIndex
                );
                results.putAll(optionResults);

                processedQuestions.put(questionId, true);

            } else {
                TestQuestionModel newQuestion = new TestQuestionModel();
                newQuestion.setText(patchQuestion.text());
                newQuestion.setOrder((byte) order);
                newQuestion.setPhase(phase);

                TestQuestionModel savedQuestion = testQuestionRepository.save(newQuestion);

                File questionImageFile = images != null && imageIndex < images.size()
                    ? images.get(imageIndex)
                    : null;
                imageIndex++;

                if (questionImageFile != null && questionImageFile.length() > 0) {
                    updateOrCreateQuestionImage(savedQuestion, questionImageFile);
                }

                Map<String, String> optionResults = processTestQuestionOptions(
                    savedQuestion,
                    patchQuestion.options(),
                    images,
                    imageIndex
                );
                results.putAll(optionResults);

                results.put("question_new_" + order, "Created with ID: " + savedQuestion.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedQuestions.entrySet()) {
            if (!entry.getValue()) {
                TestQuestionModel questionToDelete = testQuestionRepository.findById(entry.getKey()).orElse(null);
                if (questionToDelete != null) {
                    deleteQuestionWithOptions(questionToDelete);
                    results.put("question_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private Map<String, String> processTestQuestionOptions(
        TestQuestionModel question,
        List<PatchOption> options,
        List<File> images,
        int imageIndex
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
                    imageIndex++;
                    continue;
                }

                File optionImageFile = images != null && imageIndex < images.size()
                    ? images.get(imageIndex)
                    : null;
                imageIndex++;

                boolean textChanged = !Objects.equals(savedOption.getText(), patchOption.text());
                boolean correctChanged = savedOption.isCorrect() != patchOption.correct();
                boolean orderChanged = savedOption.getOrder() != order;
                boolean deleteImage = optionImageFile != null && optionImageFile.length() == 0;
                boolean replaceImage = optionImageFile != null && optionImageFile.length() > 0;

                if (textChanged || correctChanged || orderChanged || deleteImage || replaceImage) {
                    savedOption.setText(patchOption.text());
                    savedOption.setCorrect(patchOption.correct());
                    savedOption.setOrder((byte) order);

                    if (deleteImage) {
                        deleteImageFromOption(savedOption);
                    } else if (replaceImage) {
                        updateOrCreateOptionImage(savedOption, optionImageFile);
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

                File optionImageFile = images != null && imageIndex < images.size()
                    ? images.get(imageIndex)
                    : null;
                imageIndex++;

                if (optionImageFile != null && optionImageFile.length() > 0) {
                    updateOrCreateOptionImage(savedOption, optionImageFile);
                }

                results.put("question_" + question.getId() + "_option_new_" + order, "Created with ID: " + savedOption.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedOptions.entrySet()) {
            if (!entry.getValue()) {
                TestOptionModel optionToDelete = testOptionRepository.findById(entry.getKey()).orElse(null);
                if (optionToDelete != null) {
                    deleteOptionWithImage(optionToDelete);
                    results.put("question_" + question.getId() + "_option_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private void updateOrCreateQuestionImage(TestQuestionModel question, File imageFile) {
        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            ImageModel currentImage = question.getImage();

            if (currentImage != null) {
                imageService.saveImageBytes(imageBytes, currentImage.getPath());
            } else {
                String altText = "Test question image: " + (question.getText() != null ? question.getText() : "question");
                UploadImageResponse response = imageService.createAndSaveImageBytes(
                    imageBytes,
                    "test/questions",
                    imageFile.getName(),
                    altText
                );
                String path = response.url().substring(response.url().indexOf("/images/") + 1);
                ImageModel newImage = imageService.getImageModelByPath(path);
                question.setImage(newImage);
                testQuestionRepository.save(question);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing question image: " + e.getMessage(), e);
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
                testOptionRepository.save(option);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing option image: " + e.getMessage(), e);
        }
    }

    private void deleteImageFromQuestion(TestQuestionModel question) {
        ImageModel oldImage = question.getImage();
        question.setImage(null);
        testQuestionRepository.save(question);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteImageFromOption(TestOptionModel option) {
        ImageModel oldImage = option.getImage();
        option.setImage(null);
        testOptionRepository.save(option);
        if (oldImage != null) {
            imageService.deleteImage(oldImage.getPath());
        }
    }

    private void deleteGroupWithProtocol(TestGroupModel group) {
        TestProtocolModel protocol = testProtocolRepository.findByGroup(group);
        testGroupService.deleteTestGroup(group);
        if (protocol != null) {
            deleteProtocolWithPhases(protocol);
        }
    }

    private void deleteProtocolWithPhases(TestProtocolModel protocol) {
        List<TestPhaseModel> phases = testPhaseRepository.findAllByProtocol(protocol);
        for (TestPhaseModel phase : phases) {
            deletePhaseWithQuestions(phase);
        }
        testProtocolRepository.deleteById(protocol.getId());
    }

    private void deletePhaseWithQuestions(TestPhaseModel phase) {
        List<TestQuestionModel> questions = testQuestionRepository.findAllByPhase(phase);
        for (TestQuestionModel question : questions) {
            deleteQuestionWithOptions(question);
        }
        testPhaseRepository.deleteById(phase.getId());
    }

    private void deleteQuestionWithOptions(TestQuestionModel question) {
        ImageModel questionImage = question.getImage();

        List<TestOptionModel> options = testOptionRepository.findAllByQuestion(question);
        for (TestOptionModel option : options) {
            deleteOptionWithImage(option);
        }

        testQuestionRepository.deleteById(question.getId());

        if (questionImage != null) {
            imageService.deleteImage(questionImage.getPath());
        }
    }

    private void deleteOptionWithImage(TestOptionModel option) {
        ImageModel image = option.getImage();
        testOptionRepository.deleteById(option.getId());
        if (image != null) {
            imageService.deleteImage(image.getPath());
        }
    }

    private boolean checkProtocolChanged(PatchProtocol protocol, TestProtocolModel savedProtocol) {
        return (
            protocol.allowPreviousPhase() != savedProtocol.isAllowPreviousPhase() ||
                protocol.allowPreviousQuestion() != savedProtocol.isAllowPreviousQuestion() ||
                protocol.allowSkipQuestion() != savedProtocol.isAllowSkipQuestion() ||
                !Objects.equals(protocol.label(), savedProtocol.getLabel())
        );
    }

    private boolean checkChangeByGroup(PatchGroup updatedGroup, TestGroupModel savedGroup) {
        return (
            !Objects.equals(updatedGroup.label(), savedGroup.getLabel()) ||
                updatedGroup.probability() != savedGroup.getProbability() ||
                !Objects.equals(updatedGroup.greeting(), savedGroup.getGreeting())
        );
    }

    private Map<String, String> processCardInformation(List<PatchInformationCard> updateInformationCards, List<File> updateImages) {
        Map<String, String> results = new HashMap<>();
        List<HomeInfoCardModel> allExistingCards = homeInfoCardRepository.findAll();
        Map<Integer, Boolean> processedCards = new HashMap<>();
        String category = "/home/cards";

        for (HomeInfoCardModel card : allExistingCards) {
            processedCards.put(card.getId(), false);
        }

        int order = 0;
        for (PatchInformationCard patchCard : updateInformationCards) {
            File imageFile = updateImages != null ? updateImages.get(order) : null;

            if (patchCard.id().isPresent()) {
                Integer cardId = patchCard.id().get();
                HomeInfoCardModel existingCard = homeInfoCardRepository
                    .findById(cardId)
                    .orElse(null);

                if (existingCard == null) {
                    results.put("card_" + cardId, "Card not found");
                    order++;
                    continue;
                }

                boolean infoChanged = checkChangeByInfoCard(existingCard, patchCard);
                boolean deleteImage = imageFile != null && imageFile.length() == 0;
                boolean replaceImage = imageFile != null && imageFile.length() > 0;

                if (infoChanged || deleteImage || replaceImage) {
                    existingCard.setTitle(patchCard.title());
                    existingCard.setDescription(patchCard.description());
                    existingCard.setColor(patchCard.color());
                    existingCard.setOrder((byte) order);

                    if (deleteImage) {
                        ImageModel oldImage = existingCard.getIcon();
                        existingCard.setIcon(null);
                        if (oldImage != null) {
                            imageService.deleteImage(oldImage.getPath());
                        }
                    } else if (replaceImage) {
                        try {
                            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                            ImageModel currentImage = existingCard.getIcon();

                            if (currentImage != null) {
                                imageService.saveImageBytes(imageBytes, currentImage.getPath());

                            } else {
                                UploadImageResponse response = imageService.createAndSaveImageBytes(
                                    imageBytes,
                                    category,
                                    imageFile.getName(),
                                    "Home info card: " + existingCard.getTitle()
                                );
                                String path = response.url().substring(response.url().indexOf("/images/") + 1);
                                ImageModel newImage = imageService.getImageModelByPath(path);
                                existingCard.setIcon(newImage);
                            }
                        } catch (IOException e) {
                            results.put("card_" + cardId + "_image_error", "Failed to process image: " + e.getMessage());
                        }
                    }

                    homeInfoCardRepository.save(existingCard);
                    results.put("card_" + cardId, "Updated successfully");
                } else {
                    results.put("card_" + cardId, "No changes");
                }
                processedCards.put(cardId, true);

            } else {
                HomeInfoCardModel newCard = new HomeInfoCardModel();
                newCard.setTitle(patchCard.title());
                newCard.setDescription(patchCard.description());
                newCard.setColor(patchCard.color());
                newCard.setOrder((byte) order);

                if (imageFile != null && imageFile.length() > 0) {
                    try {
                        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                        UploadImageResponse response = imageService.createAndSaveImageBytes(
                            imageBytes,
                            category,
                            imageFile.getName(),
                            "Home info card: " + newCard.getTitle()
                        );
                        String path = response.url().substring(response.url().indexOf("/images/") + 1);
                        ImageModel newImage = imageService.getImageModelByPath(path);
                        newCard.setIcon(newImage);
                    } catch (IOException e) {
                        results.put("card_new_" + order + "_image_error", "Failed to process image: " + e.getMessage());
                    }
                }

                HomeInfoCardModel savedNewCard = homeInfoCardRepository.save(newCard);
                results.put("card_new_" + order, "Created with ID: " + savedNewCard.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedCards.entrySet()) {
            if (!entry.getValue()) {
                HomeInfoCardModel cardToDelete = homeInfoCardRepository.findById(entry.getKey()).orElse(null);
                if (cardToDelete != null) {
                    ImageModel imageToDelete = cardToDelete.getIcon();
                    if (imageToDelete != null) {
                        imageService.deleteImage(imageToDelete.getPath());
                    }
                    homeInfoCardRepository.deleteById(entry.getKey());
                    results.put("card_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private Map<String, String> processFaq(List<PatchFaq> updateFaqs) {
        Map<String, String> results = new HashMap<>();
        List<HomeFaqModel> allExistingFaqs = homeFaqRepository.findAll();
        Map<Integer, Boolean> processedFaqs = new HashMap<>();

        for (HomeFaqModel faq : allExistingFaqs) {
            processedFaqs.put(faq.getId(), false);
        }

        int order = 0;
        for (PatchFaq patchFaq : updateFaqs) {
            if (patchFaq.id().isPresent()) {
                Integer faqId = patchFaq.id().get();
                HomeFaqModel savedFaq = homeFaqRepository.getHomeFaqModelById(faqId);

                if (savedFaq == null) {
                    results.put("faq_" + faqId, "FAQ not found");
                    order++;
                    continue;
                }

                boolean infoChanged = checkChangeByFaq(savedFaq, patchFaq);
                boolean orderChanged = savedFaq.getOrder() != order;

                if (infoChanged || orderChanged) {
                    savedFaq.setAnswer(patchFaq.answer());
                    savedFaq.setQuestion(patchFaq.question());
                    savedFaq.setOrder((byte) order);

                    homeFaqRepository.save(savedFaq);
                    results.put("faq_" + faqId, "Updated successfully");
                } else {
                    results.put("faq_" + faqId, "No changes");
                }
                processedFaqs.put(faqId, true);

            } else {
                HomeFaqModel newFaq = new HomeFaqModel();
                newFaq.setAnswer(patchFaq.answer());
                newFaq.setQuestion(patchFaq.question());
                newFaq.setOrder((byte) order);

                HomeFaqModel savedNewFaq = homeFaqRepository.save(newFaq);
                results.put("faq_new_" + order, "Created with ID: " + savedNewFaq.getId());
            }

            order++;
        }

        for (Map.Entry<Integer, Boolean> entry : processedFaqs.entrySet()) {
            if (!entry.getValue()) {
                homeFaqRepository.deleteById(entry.getKey());
                results.put("faq_" + entry.getKey(), "Deleted");
            }
        }
        return results;
    }

    private Map<String, String> processFormQuestions(PatchForm updatedForm, List<File> images) {
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
                            continue;
                        }

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, selectMultiple);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean deleteImage = questionImageFile != null && questionImageFile.length() == 0;
                        boolean replaceImage = questionImageFile != null && questionImageFile.length() > 0;

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
                                updateOrCreateQuestionImage(savedQuestion, questionImageFile);
                            }

                            formQuestionRepository.save(savedQuestion);
                            results.put("question_" + questionId, "Updated successfully");
                        } else {
                            results.put("question_" + questionId, "No changes");
                        }

                        imageIndex = processQuestionOptions(savedQuestion, selectMultiple.options, images, imageIndex, results);
                        processedQuestions.put(questionId, true);

                    } else {
                        FormQuestionModel newQuestion = new FormQuestionModel();
                        newQuestion.setCategory(selectMultiple.category);
                        newQuestion.setText(selectMultiple.text.orElse(null));
                        newQuestion.setOrder((byte) order);
                        newQuestion.setMax((short) selectMultiple.max);
                        newQuestion.setMin((short) selectMultiple.min);
                        newQuestion.setType(FormQuestionType.SELECT_MULTIPLE);
                        newQuestion.setOther(selectMultiple.other);

                        FormQuestionModel savedNewQuestion = formQuestionRepository.save(newQuestion);

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        if (questionImageFile != null && questionImageFile.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImageFile);
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
                            continue;
                        }

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, selectOne);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean deleteImage = questionImageFile != null && questionImageFile.length() == 0;
                        boolean replaceImage = questionImageFile != null && questionImageFile.length() > 0;

                        if (infoChanged || orderChanged || deleteImage || replaceImage) {
                            savedQuestion.setCategory(selectOne.category);
                            savedQuestion.setText(selectOne.text.orElse(null));
                            savedQuestion.setOrder((byte) order);
                            savedQuestion.setOther(selectOne.other);

                            if (deleteImage) {
                                deleteImageFromEntity(savedQuestion);
                            } else if (replaceImage) {
                                updateOrCreateQuestionImage(savedQuestion, questionImageFile);
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

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        if (questionImageFile != null && questionImageFile.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImageFile);
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
                            continue;
                        }

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, slider);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean deleteImage = questionImageFile != null && questionImageFile.length() == 0;
                        boolean replaceImage = questionImageFile != null && questionImageFile.length() > 0;

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
                                updateOrCreateQuestionImage(savedQuestion, questionImageFile);
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

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        if (questionImageFile != null && questionImageFile.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImageFile);
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
                            continue;
                        }

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, textLong);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean deleteImage = questionImageFile != null && questionImageFile.length() == 0;
                        boolean replaceImage = questionImageFile != null && questionImageFile.length() > 0;

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
                                updateOrCreateQuestionImage(savedQuestion, questionImageFile);
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

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        if (questionImageFile != null && questionImageFile.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImageFile);
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
                            continue;
                        }

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        boolean infoChanged = checkChangeByQuestion(savedQuestion, textShort);
                        boolean orderChanged = savedQuestion.getOrder() != order;
                        boolean deleteImage = questionImageFile != null && questionImageFile.length() == 0;
                        boolean replaceImage = questionImageFile != null && questionImageFile.length() > 0;

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
                                updateOrCreateQuestionImage(savedQuestion, questionImageFile);
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

                        File questionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                        imageIndex++;

                        if (questionImageFile != null && questionImageFile.length() > 0) {
                            updateOrCreateQuestionImage(savedNewQuestion, questionImageFile);
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
                                       List<File> images,
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

                File optionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                imageIndex++;

                boolean textChanged = !Objects.equals(savedOption.getText(), patchOption.text().orElse(null));
                boolean orderChanged = savedOption.getOrder() != optionOrder;
                boolean deleteImage = optionImageFile != null && optionImageFile.length() == 0;
                boolean replaceImage = optionImageFile != null && optionImageFile.length() > 0;

                if (textChanged || orderChanged || deleteImage || replaceImage) {
                    savedOption.setText(patchOption.text().orElse(null));
                    savedOption.setOrder((byte) optionOrder);

                    if (deleteImage) {
                        deleteImageFromEntity(savedOption);
                    } else if (replaceImage) {
                        updateOrCreateOptionImage(savedOption, optionImageFile);
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

                File optionImageFile = images != null && imageIndex < images.size() ? images.get(imageIndex) : null;
                imageIndex++;

                if (optionImageFile != null && optionImageFile.length() > 0) {
                    updateOrCreateOptionImage(savedNewOption, optionImageFile);
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

    private boolean checkChangeByInfoCard(HomeInfoCardModel savedCard, PatchInformationCard updated) {
        return (savedCard.getColor() != updated.color() ||
            !Objects.equals(savedCard.getTitle(), updated.title()) ||
            !Objects.equals(savedCard.getDescription(), updated.description())
        );
    }

    private boolean checkChangeByFaq(HomeFaqModel savedFaq, PatchFaq updated) {
        return (!Objects.equals(savedFaq.getAnswer(), updated.answer()) ||
            !Objects.equals(savedFaq.getQuestion(), updated.question())
            );
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
