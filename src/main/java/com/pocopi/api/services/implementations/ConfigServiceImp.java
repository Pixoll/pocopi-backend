package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.dto.Form.Form;
import com.pocopi.api.dto.FormQuestion.FormQuestion;
import com.pocopi.api.dto.FormQuestionOption.FormOption;
import com.pocopi.api.dto.HomeFaq.Faq;
import com.pocopi.api.dto.HomeInfoCard.InformationCard;
import com.pocopi.api.dto.Image.Image;
import com.pocopi.api.dto.SliderLabel.SliderLabel;
import com.pocopi.api.dto.TestGroup.*;
import com.pocopi.api.models.*;
import com.pocopi.api.repositories.*;
import com.pocopi.api.services.interfaces.ConfigService;
import com.pocopi.api.services.interfaces.ImageService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImp implements ConfigService {
    private final ConfigRepository configRepository;
    private final TranslationRepository translationRepository;
    private final HomeInfoCardRepository homeInfoCardRepository;
    private final HomeFaqRepository homeFaqRepository;
    private final FormRepository formRepository;
    private final TestGroupRepository testGroupRepository;
    private final ImageService imageService;

    public ConfigServiceImp(ConfigRepository configRepository,
                            TranslationRepository translationRepository,
                            HomeInfoCardRepository homeInfoCardRepository,
                            HomeFaqRepository homeFaqRepository,
                            FormRepository formRepository,
                            ImageService imageService,
                            TestGroupRepository testGroupRepository
    ) {
        this.configRepository = configRepository;
        this.translationRepository = translationRepository;
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.homeFaqRepository = homeFaqRepository;
        this.formRepository = formRepository;
        this.imageService = imageService;
        this.testGroupRepository = testGroupRepository;
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
                String.format("#%06X", homeInfoCardModel.getColor()),
                Optional.ofNullable(iconByInfoCard)
            );
            informationCards.add(informationCard);
        }
        List<Faq> faqs = new ArrayList<>();
        for (HomeFaqModel faq : homeFaqs) {
            faqs.add(new Faq(faq.getQuestion(), faq.getAnswer()));
        }
        Map<String, Group> groups = buildGroupResponses(configId);

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
    public ConfigModel findLastConfig(){
        return configRepository.findLastConfig();
    }
    private Map<String, Group> buildGroupResponses(int configVersion) {
        List<TestGroupData> rows = testGroupRepository.findAllGroupsDataByConfigVersion(configVersion);
        if (rows.isEmpty()) {
            return Map.of();
        }

        Map<Integer, List<TestGroupData>> groupsMap =
            rows.stream().collect(Collectors.groupingBy(TestGroupData::getGroupId, LinkedHashMap::new, Collectors.toList()));

        return groupsMap.values().stream()
            .map(groupRows -> {
                TestGroupData first = groupRows.getFirst();

                Map<Integer, List<TestGroupData>> phasesMap =
                    groupRows.stream().collect(Collectors.groupingBy(TestGroupData::getPhaseId, LinkedHashMap::new, Collectors.toList()));

                List<Phase> phases = phasesMap.values().stream()
                    .map(phaseRows -> {
                        Map<Integer, List<TestGroupData>> questionsMap =
                            phaseRows.stream().collect(Collectors.groupingBy(TestGroupData::getQuestionOrder, LinkedHashMap::new, Collectors.toList()));

                        List<Question> questions = questionsMap.values().stream()
                            .map(qRows -> {
                                List<Option> options = qRows.stream()
                                    .map(r -> new Option(
                                        r.getOptionId(),
                                        r.getOptionText(),
                                        r.getOptionImageId() != null
                                            ? imageService.getImageById(r.getOptionImageId())
                                            : null,
                                        r.getCorrect()
                                    ))
                                    .toList();

                                return new Question(
                                    qRows.getFirst().getQuestionId(),
                                    qRows.getFirst().getQuestionText(),
                                    imageService.getImageById(qRows.getFirst().getQuestionImageId()),
                                    options
                                );
                            })
                            .toList();

                        return new Phase(phaseRows.getFirst().getPhaseId(),questions);
                    })
                    .toList();

                Protocol protocol = new Protocol(first.getAllowPreviousPhase(), first.getAllowPreviousQuestion(), first.getAllowSkipQuestion(), phases);
                return Map.entry(first.getGroupLabel(), new Group(
                    first.getGroupId(),
                    first.getProbability(),
                    first.getGroupLabel(),
                    first.getGreeting(),
                    protocol
                ));
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b) -> a, LinkedHashMap::new));
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

        return new Form(rows.getFirst().getFormId(),questions);
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
