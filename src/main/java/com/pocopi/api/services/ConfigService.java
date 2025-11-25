package com.pocopi.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.config.*;
import com.pocopi.api.dto.form.Form;
import com.pocopi.api.dto.test.TestGroup;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.repositories.ConfigRepository;
import com.pocopi.api.repositories.TranslationValueRepository;
import com.pocopi.api.services.ImageService.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ConfigService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ConfigRepository configRepository;
    private final TranslationValueRepository translationValueRepository;
    private final FormService formService;
    private final HomeFaqService homeFaqService;
    private final HomeInfoCardService homeInfoCardService;
    private final ImageService imageService;
    private final TestGroupService testGroupService;
    private final PatternService patternService;

    public ConfigService(
        ConfigRepository configRepository,
        TranslationValueRepository translationValueRepository,
        FormService formService,
        HomeFaqService homeFaqService,
        HomeInfoCardService homeInfoCardService,
        ImageService imageService,
        TestGroupService testGroupService,
        PatternService patternService
    ) {
        this.configRepository = configRepository;
        this.translationValueRepository = translationValueRepository;
        this.formService = formService;
        this.homeFaqService = homeFaqService;
        this.homeInfoCardService = homeInfoCardService;
        this.imageService = imageService;
        this.testGroupService = testGroupService;
        this.patternService = patternService;
    }

    @Transactional
    public List<ConfigPreview> getAllConfigs() {
        final int lastConfigVersion = configRepository.getLastConfig().getVersion();

        return configRepository.findAll().stream().map(config -> new ConfigPreview(
            config.getVersion(),
            config.getIcon() != null ? imageService.getImageById(config.getIcon().getId()) : null,
            config.getTitle(),
            config.getSubtitle(),
            config.getDescription(),
            config.isActive(),
            config.getVersion() != lastConfigVersion
                && !configRepository.hasUsersAssociatedWithConfig(config.getVersion())
        )).toList();
    }

    @Transactional
    public void deleteConfig(int version) {
        configRepository.findByVersion(version)
            .orElseThrow(() -> HttpException.notFound("Config with version " + version + " not found"));

        final int lastConfigVersion = configRepository.getLastConfig().getVersion();

        if (lastConfigVersion == version) {
            throw HttpException.conflict("Current configuration cannot be deleted");
        }

        if (configRepository.hasUsersAssociatedWithConfig(version)) {
            throw HttpException.conflict("Configuration has user data associated with it and cannot be deleted");
        }

        configRepository.deleteByVersion(version);
    }

    @Transactional
    public void setConfigAsActive(int version) {
        final ConfigModel newActiveConfig = configRepository.findByVersion(version)
            .orElseThrow(() -> HttpException.notFound("Config with version " + version + " not found"));

        final ConfigModel activeConfig = configRepository.getLastConfig();

        if (activeConfig.getVersion() == version) {
            return;
        }

        activeConfig.setActive(false);
        newActiveConfig.setActive(true);

        configRepository.save(newActiveConfig);
        configRepository.save(activeConfig);
    }

    @Transactional
    public TrimmedConfig getTrimmedActiveConfig() {
        final ConfigModel configModel = configRepository.getLastConfig();
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
    public FullConfig getFullActiveConfig() {
        final ConfigModel configModel = configRepository.getLastConfig();
        return getFullConfig(configModel);
    }

    @Transactional
    public FullConfig getFullConfigByVersion(int version) {
        final ConfigModel config = configRepository.findByVersion(version)
            .orElseThrow(() -> HttpException.notFound("Config with version " + version + " not found"));
        return getFullConfig(config);
    }

    @Transactional
    public FullConfig getFullConfig(ConfigModel configModel) {
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
    public boolean updateActiveConfig(
        ConfigUpdate configUpdate,
        MultipartFile iconFile,
        List<MultipartFile> informationCardImageFiles,
        List<MultipartFile> preTestFormImageFiles,
        List<MultipartFile> postTestFormImageFiles,
        List<MultipartFile> groupImageFiles
    ) {
        final ConfigModel storedConfig = configRepository.getLastConfig();

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

        final boolean modifiedUsernamePattern = patternService
            .updatePattern(savedConfig, configUpdate.usernamePattern());
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

    @Transactional
    public void cloneConfig(int version) {
        final ConfigModel config = configRepository.findByVersion(version)
            .orElseThrow(() -> HttpException.notFound("Config with version " + version + " not found"));

        final ImageModel newIcon = config.getIcon() != null
            ? imageService.cloneImage(config.getIcon())
            : null;

        final ConfigModel newConfig = configRepository.save(ConfigModel.builder()
            .active(false)
            .icon(newIcon)
            .title(config.getTitle())
            .subtitle(config.getSubtitle())
            .description(config.getDescription())
            .anonymous(config.isAnonymous())
            .informedConsent(config.getInformedConsent())
            .usernamePattern(config.getUsernamePattern())
            .build()
        );

        homeInfoCardService.cloneCards(version, newConfig);
        homeFaqService.cloneFaqs(version, newConfig);
        formService.cloneForms(version, newConfig);
        testGroupService.cloneGroups(version, newConfig);
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
