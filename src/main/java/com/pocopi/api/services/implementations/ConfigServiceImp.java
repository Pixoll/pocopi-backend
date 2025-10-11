package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Config.PatchLastConfig;
import com.pocopi.api.dto.Config.PatchRequest;
import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.FormQuestion.FormQuestion;
import com.pocopi.api.dto.FormQuestionOption.FormOption;
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
    private final ImageService imageService;
    private final TestGroupService testGroupService;

    public ConfigServiceImp(ConfigRepository configRepository,
                            TranslationRepository translationRepository,
                            HomeInfoCardRepository homeInfoCardRepository,
                            HomeFaqRepository homeFaqRepository,
                            FormRepository formRepository,
                            ImageService imageService,
                            TestGroupService testGroupService
    ) {
        this.configRepository = configRepository;
        this.translationRepository = translationRepository;
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.homeFaqRepository = homeFaqRepository;
        this.formRepository = formRepository;
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
    public String updateConfig(PatchRequest request) {
        return "";
    }

    private String processUpdatedConfig(PatchRequest request) {
        ConfigModel savedModel = configRepository.getByVersion(request.updateLastConfig().version());

        if (savedModel == null) {
            return "Config not found";
        }

        Map<String, String> configUpdatesSummary = processConfigGeneralData(savedModel, request.updateLastConfig());
        Map<String, String> informationCardUpdatesSummary = processCardInformation(request.updateLastConfig().informationCards(), request.informationCardFiles());
        Map<String, String> faqUpdatedSummary = processFaq(request.updateLastConfig().faq());

        return "xd";

    }

    private Map<String, String> processCardInformation(List<PatchInformationCard> updateInformationCards, List<File> updateImages) {
        Map<String, String> results = new HashMap<>();
        List<HomeInfoCardModel> allExistingCards = homeInfoCardRepository.findAll();
        Map<Integer, Boolean> processedCards = new HashMap<>();

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
                boolean orderChanged = existingCard.getOrder() != order;
                boolean deleteImage = imageFile != null && imageFile.length() == 0;
                boolean replaceImage = imageFile != null && imageFile.length() > 0;

                if (infoChanged || orderChanged || deleteImage || replaceImage) {
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
                                String category = "homeinfo";
                                String alt = "Home info card: " + existingCard.getTitle();

                                UploadImageResponse response = imageService.createAndSaveImageBytes(
                                    imageBytes,
                                    category,
                                    imageFile.getName(),
                                    alt
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
                        String category = "homeinfo";
                        String alt = "Home info card: " + newCard.getTitle();

                        UploadImageResponse response = imageService.createAndSaveImageBytes(
                            imageBytes,
                            category,
                            imageFile.getName(),
                            alt
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
