package com.pocopi.api.migration;

import com.pocopi.api.migration.entities.OldConfig;
import com.pocopi.api.migration.entities.OldConfigImage;
import com.pocopi.api.migration.entities.OldConfigTranslation;
import com.pocopi.api.migration.entities.form.OldConfigForm;
import com.pocopi.api.migration.entities.form.OldConfigFormOption;
import com.pocopi.api.migration.entities.form.OldConfigFormQuestion;
import com.pocopi.api.migration.entities.form.OldConfigFormSliderLabel;
import com.pocopi.api.migration.entities.home.OldConfigHomeFaq;
import com.pocopi.api.migration.entities.home.OldConfigHomeInfoCard;
import com.pocopi.api.migration.entities.test.*;
import com.pocopi.api.models.config.*;
import com.pocopi.api.models.form.FormModel;
import com.pocopi.api.models.form.FormQuestionModel;
import com.pocopi.api.models.form.FormQuestionOptionModel;
import com.pocopi.api.models.form.FormQuestionSliderLabelModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.*;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

@Component
public final class OldConfigMigrator {
    private final ImageRepository imageRepository;
    private final ConfigRepository configRepository;
    private final HomeInfoCardRepository homeInfoCardRepository;
    private final HomeFaqRepository homeFaqRepository;
    private final FormRepository formRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormQuestionOptionRepository formQuestionOptionRepository;
    private final FormQuestionSliderLabelRepository formQuestionSliderLabelRepository;
    private final TestGroupRepository testGroupRepository;
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final TranslationKeyRepository translationKeyRepository;
    private final TranslationValueRepository translationValueRepository;

    public OldConfigMigrator(
        ImageRepository imageRepository,
        ConfigRepository configRepository,
        HomeInfoCardRepository homeInfoCardRepository,
        HomeFaqRepository homeFaqRepository,
        FormRepository formRepository,
        FormQuestionRepository formQuestionRepository,
        FormQuestionOptionRepository formQuestionOptionRepository,
        FormQuestionSliderLabelRepository formQuestionSliderLabelRepository,
        TestGroupRepository testGroupRepository,
        TestPhaseRepository testPhaseRepository,
        TestQuestionRepository testQuestionRepository,
        TestOptionRepository testOptionRepository,
        TranslationKeyRepository translationKeyRepository,
        TranslationValueRepository translationValueRepository
    ) {
        this.imageRepository = imageRepository;
        this.configRepository = configRepository;
        this.homeInfoCardRepository = homeInfoCardRepository;
        this.homeFaqRepository = homeFaqRepository;
        this.formRepository = formRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.formQuestionOptionRepository = formQuestionOptionRepository;
        this.formQuestionSliderLabelRepository = formQuestionSliderLabelRepository;
        this.testGroupRepository = testGroupRepository;
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
        this.translationKeyRepository = translationKeyRepository;
        this.translationValueRepository = translationValueRepository;
    }

    // TODO must migrate user data as well
    public void migrate(String oldConfigPath) throws IOException {
        final LinkedHashMap<String, Object> rawConfig = OldConfigReader.read(oldConfigPath);
        final OldConfig config = OldConfigParser.parse(rawConfig);
        final HashMap<Path, ImageModel> images = new HashMap<>();

        final ConfigModel configModel = ConfigModel.builder()
            .icon(saveImage(config.icon(), images))
            .title(config.title())
            .subtitle(config.subtitle())
            .description(config.description())
            .informedConsent(config.informedConsent())
            .anonymous(config.anonymous())
            .build();

        final ConfigModel savedConfig = configRepository.save(configModel);

        for (int i = 0; i < config.informationCards().size(); i++) {
            final OldConfigHomeInfoCard informationCard = config.informationCards().get(i);

            final HomeInfoCardModel informationCardModel = HomeInfoCardModel.builder()
                .config(savedConfig)
                .order((byte) i)
                .title(informationCard.title())
                .description(informationCard.description())
                .icon(saveImage(config.icon(), images))
                .color(informationCard.color())
                .build();

            homeInfoCardRepository.save(informationCardModel);
        }

        for (int i = 0; i < config.frequentlyAskedQuestions().size(); i++) {
            final OldConfigHomeFaq frequentlyAskedQuestion = config.frequentlyAskedQuestions().get(i);

            final HomeFaqModel frequentlyAskedQuestionModel = HomeFaqModel.builder()
                .config(savedConfig)
                .order((byte) i)
                .question(frequentlyAskedQuestion.question())
                .answer(frequentlyAskedQuestion.answer())
                .build();

            homeFaqRepository.save(frequentlyAskedQuestionModel);
        }

        saveForm(savedConfig, config.preTestForm(), images);
        saveForm(savedConfig, config.postTestForm(), images);

        for (final OldConfigTestGroup group : config.groups()) {
            final TestGroupModel.TestGroupModelBuilder groupBuilder = TestGroupModel.builder()
                .config(savedConfig)
                .label(group.label())
                .probability(group.probability())
                .greeting(group.greeting());

            final OldConfigTestProtocol protocol = group.protocol();

            if (protocol != null) {
                groupBuilder
                    .allowPreviousPhase(protocol.allowPreviousPhase())
                    .allowPreviousQuestion(protocol.allowPreviousQuestion())
                    .allowSkipQuestion(protocol.allowSkipQuestion())
                    .randomizePhases(protocol.randomizePhases());
            }

            final TestGroupModel groupModel = groupBuilder.build();
            final TestGroupModel savedGroup = testGroupRepository.save(groupModel);

            if (protocol == null) {
                continue;
            }

            for (int i = 0; i < protocol.phases().size(); i++) {
                final OldConfigTestPhase phase = protocol.phases().get(i);

                final TestPhaseModel phaseModel = TestPhaseModel.builder()
                    .group(savedGroup)
                    .order((byte) i)
                    .randomizeQuestions(phase.randomizeQuestions())
                    .build();

                final TestPhaseModel savedPhase = testPhaseRepository.save(phaseModel);

                for (int j = 0; j < phase.questions().size(); j++) {
                    final OldConfigTestQuestion question = phase.questions().get(j);

                    final TestQuestionModel questionModel = TestQuestionModel.builder()
                        .phase(savedPhase)
                        .order((byte) j)
                        .text(question.text())
                        .image(saveImage(question.image(), images))
                        .randomizeOptions(question.randomizeOptions())
                        .build();

                    final TestQuestionModel savedQuestion = testQuestionRepository.save(questionModel);

                    for (int k = 0; k < question.options().size(); k++) {
                        final OldConfigTestOption option = question.options().get(k);

                        final TestOptionModel optionModel = TestOptionModel.builder()
                            .question(savedQuestion)
                            .order((byte) k)
                            .text(option.text())
                            .image(saveImage(option.image(), images))
                            .correct(option.correct())
                            .build();

                        testOptionRepository.save(optionModel);
                    }
                }
            }
        }

        for (final OldConfigTranslation translation : config.translations()) {
            final TranslationKeyModel translationKey = translationKeyRepository.getByKey(translation.key());

            if (translationKey == null) {
                continue;
            }

            final TranslationValueModel translationValueModel = TranslationValueModel.builder()
                .config(savedConfig)
                .key(translationKey)
                .value(translation.value())
                .build();

            translationValueRepository.save(translationValueModel);
        }
    }

    private void saveForm(ConfigModel config, OldConfigForm form, HashMap<Path, ImageModel> images) throws IOException {
        final FormModel formModel = FormModel.builder()
            .config(config)
            .type(form.type())
            .title(form.title())
            .build();

        final FormModel savedForm = formRepository.save(formModel);

        for (int i = 0; i < form.questions().size(); i++) {
            final OldConfigFormQuestion question = form.questions().get(i);

            final FormQuestionModel questionModel = FormQuestionModel.builder()
                .form(savedForm)
                .order((byte) i)
                .category(question.category())
                .type(question.type())
                .text(question.text())
                .image(saveImage(question.image(), images))
                .required(question.required())
                .min(question.min())
                .max(question.max())
                .step(question.step())
                .other(question.other())
                .minLength(question.minLength())
                .maxLength(question.maxLength())
                .placeholder(question.placeholder())
                .build();

            final FormQuestionModel savedQuestion = formQuestionRepository.save(questionModel);

            for (int j = 0; j < question.options().size(); j++) {
                final OldConfigFormOption option = question.options().get(j);

                final FormQuestionOptionModel optionModel = FormQuestionOptionModel.builder()
                    .formQuestion(savedQuestion)
                    .order((byte) j)
                    .text(option.text())
                    .image(saveImage(option.image(), images))
                    .build();

                formQuestionOptionRepository.save(optionModel);
            }

            for (int j = 0; j < question.labels().size(); j++) {
                final OldConfigFormSliderLabel label = question.labels().get(j);

                final FormQuestionSliderLabelModel labelModel = FormQuestionSliderLabelModel.builder()
                    .formQuestion(savedQuestion)
                    .number(label.number())
                    .label(label.label())
                    .build();

                formQuestionSliderLabelRepository.save(labelModel);
            }
        }
    }

    private ImageModel saveImage(OldConfigImage image, HashMap<Path, ImageModel> images) throws IOException {
        if (image == null) {
            return null;
        }

        if (images.containsKey(image.relativePath())) {
            return images.get(image.relativePath());
        }

        final Optional<ImageModel> storedImage = imageRepository.findByPath(image.relativePath().toString());

        if (storedImage.isPresent()) {
            images.put(image.relativePath(), storedImage.get());
            return storedImage.get();
        }

        final Path destination = Paths.get("images", image.relativePath().toString());
        final Path parentDir = destination.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }

        Files.copy(image.absolutePath(), destination, StandardCopyOption.REPLACE_EXISTING);

        final ImageModel imageModel = ImageModel.builder()
            .path(image.relativePath().toString())
            .alt(image.alt())
            .build();

        final ImageModel savedImage = imageRepository.save(imageModel);
        images.put(image.relativePath(), savedImage);

        return savedImage;
    }
}
