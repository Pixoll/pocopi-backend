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
import com.pocopi.api.models.form.FormQuestionType;
import com.pocopi.api.models.form.FormType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public final class OldConfigParser {
    public static OldConfig parse(LinkedHashMap<String, Object> rawConfig) {
        @SuppressWarnings("unchecked")
        final HashMap<String, Path> imagePaths = castNonNull(
            rawConfig.get("images"),
            HashMap.class,
            "Config images must be a dictionary"
        );

        final OldConfigImage icon = parseImage(rawConfig.get("icon"), imagePaths);
        final String title = castNonNull(rawConfig.get("title"), String.class, "Config title must be a string");
        final String subtitle = cast(rawConfig.get("subtitle"), String.class, "Config subtitle must be a string");
        final String description = castNonNull(
            rawConfig.get("description"),
            String.class,
            "Config description must be a string"
        );
        final boolean anonymous = cast(
            rawConfig.get("anonymous"),
            Boolean.class,
            true,
            "Config anonymous must be a boolean"
        );
        final ArrayList<OldConfigHomeInfoCard> informationCards = parseInformationCards(
            rawConfig.get("informationCards"),
            imagePaths
        );
        final String informedConsent = castNonNull(
            rawConfig.get("informedConsent"),
            String.class,
            "Config informed consent must be a string"
        );
        final ArrayList<OldConfigHomeFaq> frequentlyAskedQuestions = parseFrequentlyAskedQuestions(
            rawConfig.get("faq")
        );
        final OldConfigForm preTestForm = parseForm(rawConfig.get("preTestForm"), FormType.PRE, imagePaths);
        final OldConfigForm postTestForm = parseForm(rawConfig.get("postTestForm"), FormType.POST, imagePaths);
        final ArrayList<OldConfigTestGroup> groups = parseTestGroups(
            rawConfig.get("groups"),
            rawConfig.get("protocols"),
            rawConfig.get("phases"),
            rawConfig.get("questions"),
            imagePaths
        );
        final ArrayList<OldConfigTranslation> translations = parseTranslations(rawConfig.get("translations"));

        return new OldConfig(
            icon,
            title,
            subtitle,
            description,
            anonymous,
            informationCards,
            informedConsent,
            frequentlyAskedQuestions,
            preTestForm,
            postTestForm,
            groups,
            translations
        );
    }

    private static ArrayList<OldConfigHomeInfoCard> parseInformationCards(
        Object rawInformationCards,
        HashMap<String, Path> imagePaths
    ) {
        final ArrayList<?> rawInformationCardsList = cast(
            rawInformationCards,
            ArrayList.class,
            new ArrayList<>(),
            "Information cards must be a list"
        );

        return rawInformationCardsList.stream().map(rawInformationCard -> {
            final LinkedHashMap<?, ?> informationCardMap = castNonNull(
                rawInformationCard,
                LinkedHashMap.class,
                "Information card must be a dictionary"
            );

            final LinkedHashMap<?, ?> colorMap = cast(
                informationCardMap.get("color"),
                LinkedHashMap.class,
                new LinkedHashMap<>(),
                "Information card color must be a dictionary"
            );

            final String title = castNonNull(
                informationCardMap.get("title"),
                String.class,
                "Inforamtion card title must be a string"
            );
            final String desctiption = castNonNull(
                informationCardMap.get("description"),
                String.class,
                "Inforamtion card description must be a string"
            );
            final OldConfigImage image = parseImage(informationCardMap.get("image"), imagePaths);

            final Integer red = cast(colorMap.get("r"), Integer.class, "Color red must be an integer");
            final Integer green = cast(colorMap.get("g"), Integer.class, "Color green must be an integer");
            final Integer blue = cast(colorMap.get("b"), Integer.class, "Color blue must be an integer");

            final Integer color = red != null && green != null && blue != null
                ? (red << 16) + (green << 8) + blue
                : null;

            return new OldConfigHomeInfoCard(title, desctiption, image, color);
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private static ArrayList<OldConfigHomeFaq> parseFrequentlyAskedQuestions(Object rawFrequentlyAskedQuestions) {
        final ArrayList<?> rawFrequentlyAskedQuestionsList = cast(
            rawFrequentlyAskedQuestions,
            ArrayList.class,
            new ArrayList<>(),
            "Frequently asked questions must be a list"
        );

        return rawFrequentlyAskedQuestionsList.stream().map(rawFrequentlyAskedQuestion -> {
            final LinkedHashMap<?, ?> frequentlyAskedQuestionMap = castNonNull(
                rawFrequentlyAskedQuestion,
                LinkedHashMap.class,
                "Frequently asked questions must be a dictionary"
            );

            final String question = castNonNull(
                frequentlyAskedQuestionMap.get("question"),
                String.class,
                "FAQ question must be a string"
            );
            final String answer = castNonNull(
                frequentlyAskedQuestionMap.get("answer"),
                String.class,
                "FAQ answer must be a string"
            );

            return new OldConfigHomeFaq(question, answer);
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private static OldConfigForm parseForm(Object rawForm, FormType type, HashMap<String, Path> imagePaths) {
        if (rawForm == null) {
            return null;
        }

        final LinkedHashMap<?, ?> formMap = castNonNull(rawForm, LinkedHashMap.class, "Form must be a dictionary");

        final ArrayList<?> rawQuestionsList = castNonNull(
            formMap.get("questions"),
            ArrayList.class,
            "Form questions must be a list"
        );

        final String title = cast(formMap.get("title"), String.class, "Form title must be a string");

        final ArrayList<OldConfigFormQuestion> questions = rawQuestionsList.stream()
            .map(rawQuestion -> parseFormQuestion(rawQuestion, imagePaths))
            .collect(Collectors.toCollection(ArrayList::new));

        return new OldConfigForm(title, type, questions);
    }

    private static OldConfigFormQuestion parseFormQuestion(Object rawQuestion, HashMap<String, Path> imagePaths) {
        final LinkedHashMap<?, ?> questionMap = castNonNull(
            rawQuestion,
            LinkedHashMap.class,
            "Form question must be a dictionary"
        );

        final String category = castNonNull(
            questionMap.get("category"),
            String.class,
            "Form question category must be a string"
        );
        final FormQuestionType type = FormQuestionType.fromValue(castNonNull(
            questionMap.get("type"),
            String.class,
            "Form question type must be a string"
        ));
        final String text = castNonNull(questionMap.get("text"), String.class, "Form question text must be a string");
        final OldConfigImage image = parseImage(questionMap.get("image"), imagePaths);
        final boolean required = cast(
            questionMap.get("required"),
            Boolean.class,
            true,
            "Form question required must be a boolean"
        );
        final Integer min = cast(questionMap.get("min"), Integer.class, "Form question min must be an integer");
        final Integer max = cast(questionMap.get("max"), Integer.class, "Form question max must be an integer");
        final Integer step = cast(questionMap.get("step"), Integer.class, "Form question step must be an integer");
        final Boolean other = cast(questionMap.get("other"), Boolean.class, "Form question other must be a boolean");
        final Integer minLength = cast(
            questionMap.get("minLength"),
            Integer.class,
            "Form question min length must be an integer"
        );
        final Integer maxLength = cast(
            questionMap.get("maxLength"),
            Integer.class,
            "Form question max length must be an integer"
        );
        final String placeholder = cast(
            questionMap.get("placeholder"),
            String.class,
            "Form question placeholder must be a string"
        );
        final ArrayList<OldConfigFormOption> options = parseFormOptions(questionMap.get("options"), imagePaths);
        final ArrayList<OldConfigFormSliderLabel> labels = parseFormQuestionSliderLabels(questionMap.get("labels"));

        return new OldConfigFormQuestion(
            category,
            type,
            text,
            image,
            required,
            min,
            max,
            step,
            other,
            minLength,
            maxLength,
            placeholder,
            options,
            labels
        );
    }

    private static ArrayList<OldConfigFormSliderLabel> parseFormQuestionSliderLabels(Object rawLabels) {
        final LinkedHashMap<?, ?> labelsMap = cast(
            rawLabels,
            LinkedHashMap.class,
            new LinkedHashMap<>(),
            "Form question slider labels must be a dictionary"
        );

        final ArrayList<OldConfigFormSliderLabel> sliderLabels = new ArrayList<>();

        labelsMap.forEach((rawNumber, rawLabel) -> {
            final int number = castNonNull(
                rawNumber,
                Integer.class,
                "Form question slider label number must be an integer"
            );
            final String label = castNonNull(rawLabel, String.class, "Form question slider label must be a string");

            final OldConfigFormSliderLabel sliderLabel = new OldConfigFormSliderLabel(number, label);

            sliderLabels.add(sliderLabel);
        });

        return sliderLabels;
    }

    private static ArrayList<OldConfigFormOption> parseFormOptions(
        Object rawOptions,
        HashMap<String, Path> imagePaths
    ) {
        final ArrayList<?> rawOptionsList = cast(
            rawOptions,
            ArrayList.class,
            new ArrayList<>(),
            "Form options must be a list"
        );

        return rawOptionsList.stream().map(rawOption -> {
            final LinkedHashMap<?, ?> optionMap = castNonNull(
                rawOption,
                LinkedHashMap.class,
                "Form option must be a dictionary"
            );

            final String text = cast(optionMap.get("text"), String.class, "Form option test must be a string");
            final OldConfigImage image = parseImage(optionMap.get("image"), imagePaths);

            return new OldConfigFormOption(text, image);
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private static ArrayList<OldConfigTestGroup> parseTestGroups(
        Object rawGroups,
        Object rawProtocols,
        Object rawPhases,
        Object rawQuestions,
        HashMap<String, Path> imagePaths
    ) {
        final HashMap<String, OldConfigTestQuestion> questionsMap = parseTestQuestions(rawQuestions, imagePaths);
        final HashMap<String, OldConfigTestPhase> phasesMap = parseTestPhases(rawPhases, questionsMap, imagePaths);
        final HashMap<String, OldConfigTestProtocol> protocolsMap = parseTestProtocols(
            rawProtocols,
            phasesMap,
            questionsMap,
            imagePaths
        );

        final LinkedHashMap<?, ?> groupsMap = cast(
            rawGroups,
            LinkedHashMap.class,
            new LinkedHashMap<>(),
            "Test groups must be a dictionary"
        );

        final ArrayList<OldConfigTestGroup> groups = new ArrayList<>();

        groupsMap.forEach((rawGroupLabel, rawGroup) -> {
            final String groupLabel = castNonNull(rawGroupLabel, String.class, "Test group label must be a string");
            final OldConfigTestGroup group = parseTestGroup(
                groupLabel,
                rawGroup,
                protocolsMap,
                phasesMap,
                questionsMap,
                imagePaths
            );

            groups.add(group);
        });

        return groups;
    }

    private static OldConfigTestGroup parseTestGroup(
        String label,
        Object rawGroup,
        HashMap<String, OldConfigTestProtocol> protocolsMap,
        HashMap<String, OldConfigTestPhase> phasesMap,
        HashMap<String, OldConfigTestQuestion> questionsMap,
        HashMap<String, Path> imagePaths
    ) {
        final LinkedHashMap<?, ?> groupMap = castNonNull(
            rawGroup,
            LinkedHashMap.class,
            "Test group must be a dictionary"
        );

        final Object rawProtocol = groupMap.get("protocol");

        final Double decimalProbability = castNonNull(
            groupMap.get("probability"),
            Double.class,
            "Test group probability must be a number"
        );

        final byte probability = (byte) Math.round(decimalProbability * 100);

        final String greeting = cast(groupMap.get("greeting"), String.class, "Test group greeting must be a string");

        final OldConfigTestProtocol protocol;

        switch (rawProtocol) {
            case null -> protocol = null;

            case String protocolLabel -> {
                protocol = protocolsMap.get(protocolLabel);
                if (protocol == null) {
                    throw new IllegalArgumentException("Test protocol with label " + protocolLabel + " not found");
                }
            }

            case LinkedHashMap<?, ?> protocolMap ->
                protocol = parseTestProtocol(label, protocolMap, phasesMap, questionsMap, imagePaths);

            default -> throw new IllegalArgumentException("Test group protocol must be a string label or dictionary");
        }

        return new OldConfigTestGroup(label, probability, greeting, protocol);
    }

    private static HashMap<String, OldConfigTestProtocol> parseTestProtocols(
        Object rawProtocols,
        HashMap<String, OldConfigTestPhase> phasesMap,
        HashMap<String, OldConfigTestQuestion> questionsMap,
        HashMap<String, Path> imagePaths
    ) {
        final LinkedHashMap<?, ?> protocolsMap = cast(
            rawProtocols,
            LinkedHashMap.class,
            new LinkedHashMap<>(),
            "Test protocols must be a dictionary"
        );

        final HashMap<String, OldConfigTestProtocol> protocols = new HashMap<>();

        protocolsMap.forEach((rawProtocolLabel, rawProtocol) -> {
            final String protocolLabel = castNonNull(
                rawProtocolLabel,
                String.class,
                "Test protocol label must be a string"
            );
            final OldConfigTestProtocol protocol = parseTestProtocol(
                protocolLabel,
                rawProtocol,
                phasesMap,
                questionsMap,
                imagePaths
            );

            protocols.put(protocolLabel, protocol);
        });

        return protocols;
    }

    private static OldConfigTestProtocol parseTestProtocol(
        String label,
        Object rawProtocol,
        HashMap<String, OldConfigTestPhase> phasesMap,
        HashMap<String, OldConfigTestQuestion> questionsMap,
        HashMap<String, Path> imagePaths
    ) {
        final LinkedHashMap<?, ?> protocolMap = castNonNull(
            rawProtocol,
            LinkedHashMap.class,
            "Test protocol must be a dictionary"
        );

        final ArrayList<?> rawPhasesList = castNonNull(
            protocolMap.get("phases"),
            ArrayList.class,
            "Test protocol phases must be a list"
        );

        final boolean randomizePhases = cast(
            protocolMap.get("randomize"),
            Boolean.class,
            false,
            "Test protocol randomize must be a boolean"
        );
        final boolean allowPreviousPhase = cast(
            protocolMap.get("allowPreviousPhase"),
            Boolean.class,
            true,
            "Test protocol allow previous phase must be a boolean"
        );
        final boolean allowPreviousQuestion = cast(
            protocolMap.get("allowPreviousQuestion"),
            Boolean.class,
            true,
            "Test protocol allow previous question must be a boolean"
        );
        final boolean allowSkipQuestion = cast(
            protocolMap.get("allowSkipQuestion"),
            Boolean.class,
            true,
            "Test protocol skip question must be a boolean"
        );

        final ArrayList<OldConfigTestPhase> phases = rawPhasesList.stream().map(rawPhase ->
            switch (rawPhase) {
                case String phaseLabel -> {
                    final OldConfigTestPhase phase = phasesMap.get(phaseLabel);
                    if (phase == null) {
                        throw new IllegalArgumentException("Test phase with label " + phaseLabel + " not found");
                    }
                    yield phase;
                }

                case LinkedHashMap<?, ?> phaseMap -> parseTestPhase(phaseMap, questionsMap, imagePaths);

                default ->
                    throw new IllegalArgumentException("Test protocol phase must be a string label or dictionary");
            }
        ).collect(Collectors.toCollection(ArrayList::new));

        return new OldConfigTestProtocol(
            label,
            phases,
            randomizePhases,
            allowPreviousPhase,
            allowPreviousQuestion,
            allowSkipQuestion
        );
    }

    private static HashMap<String, OldConfigTestPhase> parseTestPhases(
        Object rawPhases,
        HashMap<String, OldConfigTestQuestion> questionsMap,
        HashMap<String, Path> imagePaths
    ) {
        final LinkedHashMap<?, ?> phasesMap = cast(
            rawPhases,
            LinkedHashMap.class,
            new LinkedHashMap<>(),
            "Test phases must be a dictionary"
        );

        final HashMap<String, OldConfigTestPhase> phases = new HashMap<>();

        phasesMap.forEach((rawPhaseLabel, rawPhase) -> {
            final String phaseLabel = castNonNull(rawPhaseLabel, String.class, "Test phase label must be a string");
            final OldConfigTestPhase phase = parseTestPhase(rawPhase, questionsMap, imagePaths);

            phases.put(phaseLabel, phase);
        });

        return phases;
    }

    private static OldConfigTestPhase parseTestPhase(
        Object rawPhase,
        HashMap<String, OldConfigTestQuestion> questionsMap,
        HashMap<String, Path> imagePaths
    ) {
        final LinkedHashMap<?, ?> phaseMap = castNonNull(
            rawPhase,
            LinkedHashMap.class,
            "Test phase must be a dictionary"
        );

        final ArrayList<?> rawQuestionsList = castNonNull(
            phaseMap.get("questions"),
            ArrayList.class,
            "Test phase questions must be a list"
        );

        final boolean randomizeQuestions = cast(
            phaseMap.get("randomize"),
            Boolean.class,
            false,
            "Test phase randomize must be a boolean"
        );

        final ArrayList<OldConfigTestQuestion> questions = rawQuestionsList.stream().map(rawQuestion ->
            switch (rawQuestion) {
                case String questionLabel -> {
                    final OldConfigTestQuestion question = questionsMap.get(questionLabel);
                    if (question == null) {
                        throw new IllegalArgumentException("Test question with label " + questionLabel + " not found");
                    }
                    yield question;
                }

                case LinkedHashMap<?, ?> questionMap -> parseTestQuestion(questionMap, imagePaths);

                default ->
                    throw new IllegalArgumentException("Test phase question must be a string label or dictionary");
            }
        ).collect(Collectors.toCollection(ArrayList::new));

        return new OldConfigTestPhase(questions, randomizeQuestions);
    }

    private static HashMap<String, OldConfigTestQuestion> parseTestQuestions(
        Object rawQuestions,
        HashMap<String, Path> imagePaths
    ) {
        final LinkedHashMap<?, ?> questionsMap = cast(
            rawQuestions,
            LinkedHashMap.class,
            new LinkedHashMap<>(),
            "Test questions must be a dictionary"
        );

        final HashMap<String, OldConfigTestQuestion> questions = new HashMap<>();

        questionsMap.forEach((rawQuestionLabel, rawQuestion) -> {
            final String questionLabel = castNonNull(
                rawQuestionLabel,
                String.class,
                "Test question label must be a string"
            );
            final OldConfigTestQuestion question = parseTestQuestion(rawQuestion, imagePaths);

            questions.put(questionLabel, question);
        });

        return questions;
    }

    private static OldConfigTestQuestion parseTestQuestion(Object rawQuestion, HashMap<String, Path> imagePaths) {
        final LinkedHashMap<?, ?> questionMap = castNonNull(
            rawQuestion,
            LinkedHashMap.class,
            "Test question must be a dictionary"
        );

        final String text = cast(questionMap.get("text"), String.class, "Test question test must be a string");
        final OldConfigImage image = parseImage(questionMap.get("image"), imagePaths);
        final ArrayList<OldConfigTestOption> options = parseTestOptions(questionMap.get("options"), imagePaths);
        final boolean randomizeOptions = cast(
            questionMap.get("randomize"),
            Boolean.class,
            false,
            "Test question randomize must be a boolean"
        );

        return new OldConfigTestQuestion(text, image, options, randomizeOptions);
    }

    private static ArrayList<OldConfigTestOption> parseTestOptions(
        Object rawOptions,
        HashMap<String, Path> imagePaths
    ) {
        final ArrayList<?> rawOptionsList = castNonNull(rawOptions, ArrayList.class, "Test options must be a list");

        return rawOptionsList.stream().map(rawOption -> {
            final LinkedHashMap<?, ?> optionMap = castNonNull(
                rawOption,
                LinkedHashMap.class,
                "Test option must be a dictionary"
            );

            final String text = cast(optionMap.get("text"), String.class, "Test option test must be a string");
            final OldConfigImage image = parseImage(optionMap.get("image"), imagePaths);
            final boolean correct = cast(
                optionMap.get("correct"),
                Boolean.class,
                false,
                "Test option correct must be a boolean"
            );

            return new OldConfigTestOption(text, image, correct);
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private static OldConfigImage parseImage(Object rawImage, HashMap<String, Path> imagePaths) {
        if (rawImage == null) {
            return null;
        }

        final LinkedHashMap<?, ?> imageMap = castNonNull(rawImage, LinkedHashMap.class, "Image must be a dictionary");
        final String pathString = castNonNull(imageMap.get("src"), String.class, "Image path must be a string");
        final String alt = castNonNull(imageMap.get("alt"), String.class, "Image alt must be a string");
        final Path path = imagePaths.get(pathString);

        if (path == null) {
            throw new IllegalArgumentException("Image path " + pathString + " not found");
        }

        return new OldConfigImage(Paths.get(pathString), path, alt);
    }

    private static ArrayList<OldConfigTranslation> parseTranslations(Object rawTranslations) {
        final LinkedHashMap<?, ?> translationsMap = cast(
            rawTranslations,
            LinkedHashMap.class,
            new LinkedHashMap<>(),
            "Translations must be a dictionary"
        );

        final ArrayList<OldConfigTranslation> translations = new ArrayList<>();

        translationsMap.forEach((rawKey, rawValue) -> {
            final String key = castNonNull(rawKey, String.class, "Translations key must be string");

            switch (rawValue) {
                case String value -> translations.add(new OldConfigTranslation(key, value));

                case LinkedHashMap<?, ?> map -> map.forEach((rawSubkey, rawSubvalue) -> {
                    final String subkey = castNonNull(
                        rawSubkey,
                        String.class,
                        "Translations subkey must be string"
                    );
                    final String subvalue = castNonNull(
                        rawSubvalue,
                        String.class,
                        "Translations subvalue must be string"
                    );

                    translations.add(new OldConfigTranslation(key + "." + subkey, subvalue));
                });

                default ->
                    throw new IllegalArgumentException("Translations value must be either a string or a dictionary");
            }
        });

        return translations;
    }

    private static <T> T castNonNull(Object object, Class<T> clazz, String errorMessage) {
        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }

        throw new IllegalArgumentException(errorMessage);
    }

    private static <T> T cast(Object object, Class<T> clazz, String errorMessage) {
        if (object == null) {
            return null;
        }

        return castNonNull(object, clazz, errorMessage);
    }

    private static <T> T cast(Object object, Class<T> clazz, T defaultValue, String errorMessage) {
        if (object == null) {
            return defaultValue;
        }

        return castNonNull(object, clazz, errorMessage);
    }
}
