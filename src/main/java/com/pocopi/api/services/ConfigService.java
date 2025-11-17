package com.pocopi.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.api.FieldError;
import com.pocopi.api.dto.config.*;
import com.pocopi.api.dto.form.Form;
import com.pocopi.api.dto.test.TestGroup;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.config.PatternModel;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.PatternRepository;
import com.pocopi.api.repositories.TranslationValueRepository;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;

@Service
public class ConfigService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ConfigRepository configRepository;
    private final PatternRepository patternRepository;
    private final TranslationValueRepository translationValueRepository;
    private final FormService formService;
    private final HomeFaqService homeFaqService;
    private final HomeInfoCardService homeInfoCardService;
    private final ImageService imageService;
    private final TestGroupService testGroupService;

    public ConfigService(
        ConfigRepository configRepository,
        PatternRepository patternRepository,
        TranslationValueRepository translationValueRepository,
        FormService formService,
        HomeFaqService homeFaqService,
        HomeInfoCardService homeInfoCardService,
        ImageService imageService,
        TestGroupService testGroupService
    ) {
        this.configRepository = configRepository;
        this.patternRepository = patternRepository;
        this.translationValueRepository = translationValueRepository;
        this.formService = formService;
        this.homeFaqService = homeFaqService;
        this.homeInfoCardService = homeInfoCardService;
        this.imageService = imageService;
        this.testGroupService = testGroupService;
    }

    @Transactional
    public List<ConfigPreview> getAllConfigs() {
        final int lastConfigVersion = configRepository.findLastConfig().getVersion();

        return configRepository.findAll().stream().map(config -> new ConfigPreview(
            config.getVersion(),
            config.getIcon() != null ? imageService.getImageById(config.getIcon().getId()) : null,
            config.getTitle(),
            config.getSubtitle(),
            config.getDescription(),
            config.getVersion() != lastConfigVersion
                && !configRepository.hasUsersAssociatedWithConfig(config.getVersion())
        )).toList();
    }

    @Transactional
    public void deleteConfig(int version) {
        if (configRepository.hasUsersAssociatedWithConfig(version)) {
            throw HttpException.conflict("Configuration has user data associated with it and cannot be deleted");
        }

        configRepository.deleteByVersion(version);
    }

    @Transactional
    public TrimmedConfig getLatestConfigTrimmed() {
        final ConfigModel configModel = configRepository.findLastConfig();
        final int configVersion = configModel.getVersion();

        final Image icon = configModel.getIcon() != null
            ? imageService.getImageById(configModel.getIcon().getId())
            : null;

        final Pattern usernamePattern = configModel.getUsernamePattern() != null ? new Pattern(
            configModel.getUsernamePattern().getId(),
            configModel.getUsernamePattern().getName(),
            configModel.getUsernamePattern().getRegex()
        ) : null;

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

        final List<InformationCard> informationCards = homeInfoCardService.getCardsByConfigVersion(configVersion);
        final List<FrequentlyAskedQuestion> frequentlyAskedQuestions = homeFaqService
            .getFaqsByConfigVersion(configVersion);

        return new TrimmedConfig(
            icon,
            configModel.getTitle(),
            configModel.getSubtitle(),
            configModel.getDescription(),
            configModel.isAnonymous(),
            usernamePattern,
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

        final Pattern usernamePattern = configModel.getUsernamePattern() != null ? new Pattern(
            configModel.getUsernamePattern().getId(),
            configModel.getUsernamePattern().getName(),
            configModel.getUsernamePattern().getRegex()
        ) : null;

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

        final List<InformationCard> informationCards = homeInfoCardService.getCardsByConfigVersion(configVersion);
        final List<FrequentlyAskedQuestion> frequentlyAskedQuestions = homeFaqService
            .getFaqsByConfigVersion(configVersion);

        return new FullConfig(
            configVersion,
            icon,
            configModel.getTitle(),
            configModel.getSubtitle(),
            configModel.getDescription(),
            configModel.isAnonymous(),
            usernamePattern,
            informationCards,
            configModel.getInformedConsent(),
            frequentlyAskedQuestions,
            preTest,
            postTest,
            groups,
            translations
        );
    }

    @Transactional
    public boolean updateLatestConfig(
        ConfigUpdate configUpdate,
        MultipartFile iconFile,
        List<MultipartFile> informationCardImageFiles,
        List<MultipartFile> preTestFormImageFiles,
        List<MultipartFile> postTestFormImageFiles,
        List<MultipartFile> groupImageFiles
    ) {
        final ConfigModel storedConfig = configRepository.findLastConfig();

        if (!configUpdate.groups().isEmpty()) {
            final int probabilitySum = configUpdate.groups().stream()
                .reduce(0, (subtotal, group) -> subtotal + group.probability(), Integer::sum);

            if (probabilitySum != 100) {
                throw HttpException.badRequest("Config groups probability sum must be 100, got " + probabilitySum);
            }
        }

        final boolean modifiedGeneral = !Objects.equals(storedConfig.getTitle(), configUpdate.title())
            || !Objects.equals(storedConfig.getSubtitle(), configUpdate.subtitle())
            || !Objects.equals(storedConfig.getDescription(), configUpdate.description())
            || storedConfig.isAnonymous() != configUpdate.anonymous()
            || !Objects.equals(storedConfig.getInformedConsent(), configUpdate.informedConsent())
            || iconFile != null;

        final ConfigModel savedConfig;

        if (modifiedGeneral) {
            final ImageModel storedIcon = storedConfig.getIcon();

            storedConfig.setTitle(configUpdate.title());
            storedConfig.setSubtitle(configUpdate.subtitle());
            storedConfig.setDescription(configUpdate.description());
            storedConfig.setAnonymous(configUpdate.anonymous());
            storedConfig.setInformedConsent(configUpdate.informedConsent());

            if (iconFile != null) {
                if (iconFile.isEmpty()) {
                    storedConfig.setIcon(null);
                } else if (storedIcon == null) {
                    final ImageModel newIcon = imageService.saveImageFile(
                        ImageCategory.ICON,
                        iconFile,
                        "Application icon"
                    );
                    storedConfig.setIcon(newIcon);
                } else {
                    imageService.updateImageFile(ImageCategory.ICON, storedIcon, iconFile);
                }
            }

            savedConfig = configRepository.save(storedConfig);

            if (storedIcon != null && storedConfig.getIcon() == null) {
                imageService.deleteImageIfUnused(storedIcon);
            }
        } else {
            savedConfig = storedConfig;
        }

        boolean modifiedUsernamePattern = false;

        if (configUpdate.usernamePattern() == null) {
            if (savedConfig.getUsernamePattern() != null) {
                savedConfig.setUsernamePattern(null);

                configRepository.save(savedConfig);
                modifiedUsernamePattern = true;
            }
        } else {
            final PatternUpdate patternUpdate = configUpdate.usernamePattern();

            try {
                java.util.regex.Pattern.compile(patternUpdate.regex());
            } catch (PatternSyntaxException e) {
                throw new MultiFieldException(
                    "Invalid configuration update",
                    List.of(new FieldError("usernamePattern", "Invalid Java matching pattern"))
                );
            }

            final PatternModel patternModel = patternUpdate.id() != null
                ? patternRepository.findById(patternUpdate.id()).orElse(null)
                : null;

            if (patternModel == null) {
                final PatternModel newPattern = PatternModel.builder()
                    .name(patternUpdate.name())
                    .regex(patternUpdate.regex())
                    .build();

                final PatternModel savedPattern = patternRepository.save(newPattern);

                savedConfig.setUsernamePattern(savedPattern);

                configRepository.save(savedConfig);
                modifiedUsernamePattern = true;
            } else if (
                !Objects.equals(patternModel.getName(), patternUpdate.name())
                    || !Objects.equals(patternModel.getRegex(), patternUpdate.regex())
            ) {
                patternModel.setName(patternModel.getName());
                patternModel.setRegex(patternUpdate.regex());

                patternRepository.save(patternModel);
                modifiedUsernamePattern = true;
            }
        }

        final boolean modifiedCards = homeInfoCardService.updateCards(
            savedConfig,
            configUpdate.informationCards(),
            informationCardImageFiles
        );
        final boolean modifiedFaq = homeFaqService.updateFaqs(savedConfig, configUpdate.faq());
        final boolean modifiedPreTestForm = formService.updateForm(
            savedConfig,
            FormType.PRE,
            configUpdate.preTestForm(),
            preTestFormImageFiles
        );
        final boolean modifiedPostTestForm = formService.updateForm(
            savedConfig,
            FormType.POST,
            configUpdate.postTestForm(),
            postTestFormImageFiles
        );
        final boolean modifiedGroups = testGroupService.updateGroups(
            savedConfig,
            configUpdate.groups(),
            groupImageFiles
        );

        return modifiedGeneral
            || modifiedUsernamePattern
            || modifiedCards
            || modifiedFaq
            || modifiedPreTestForm
            || modifiedPostTestForm
            || modifiedGroups;
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
