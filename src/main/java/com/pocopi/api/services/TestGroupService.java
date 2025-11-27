package com.pocopi.api.services;

import com.pocopi.api.dto.config.Image;
import com.pocopi.api.dto.test.*;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.ImageModel;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TestGroupService {
    private final TestGroupRepository testGroupRepository;
    private final ConfigRepository configRepository;
    private final TestPhaseService testPhaseService;
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final ImageService imageService;
    private final UserTestOptionLogRepository userTestOptionLogRepository;
    private final UserTestQuestionLogRepository userTestQuestionLogRepository;
    private final UserTestAttemptRepository userTestAttemptRepository;

    public TestGroupService(
        TestGroupRepository testGroupRepository,
        ConfigRepository configRepository,
        TestPhaseService testPhaseService,
        TestPhaseRepository testPhaseRepository,
        TestQuestionRepository testQuestionRepository,
        TestOptionRepository testOptionRepository,
        ImageService imageService,
        UserTestOptionLogRepository userTestOptionLogRepository,
        UserTestQuestionLogRepository userTestQuestionLogRepository,
        UserTestAttemptRepository userTestAttemptRepository
    ) {
        this.testGroupRepository = testGroupRepository;
        this.configRepository = configRepository;
        this.testPhaseService = testPhaseService;
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
        this.imageService = imageService;
        this.userTestOptionLogRepository = userTestOptionLogRepository;
        this.userTestQuestionLogRepository = userTestQuestionLogRepository;
        this.userTestAttemptRepository = userTestAttemptRepository;
    }

    public TestGroupModel sampleGroup() {
        final int configVersion = configRepository.getLastConfig().getVersion();

        final List<TestGroupModel> groups = testGroupRepository.findAllByConfigVersion(configVersion);

        final ArrayList<Integer> probabilitySums = new ArrayList<>();
        int lastProbability = 100;

        for (final TestGroupModel group : groups) {
            lastProbability -= group.getProbability();
            probabilitySums.add(lastProbability);
        }

        if (lastProbability != 0) {
            throw new IllegalArgumentException("The sum of all group probabilities should be 100");
        }

        final SecureRandom random = new SecureRandom();
        final int targetProbability = random.nextInt(100);

        for (int i = 0; i < probabilitySums.size(); i++) {
            if (targetProbability > probabilitySums.get(i)) {
                return groups.get(i);
            }
        }

        return groups.getFirst();
    }

    @Transactional
    public AssignedTestGroup getAssignedGroup(TestGroupModel groupModel) {
        final List<TestPhaseModel> phasesList = testPhaseRepository.findAllByGroupIdOrderByOrder(groupModel.getId());
        final List<TestQuestionModel> questionsList = testQuestionRepository
            .findAllByPhaseGroupIdOrderByOrder(groupModel.getId());
        final List<TestOptionModel> optionsList = testOptionRepository
            .findAllByQuestionPhaseGroupIdOrderByOrder(groupModel.getId());

        final HashMap<Integer, ArrayList<TestQuestionModel>> phaseIdToQuestionsMap = new HashMap<>();
        final HashMap<Integer, ArrayList<TestOptionModel>> questionIdToOptionsMap = new HashMap<>();

        for (final TestQuestionModel question : questionsList) {
            final int phaseId = question.getPhase().getId();
            final ArrayList<TestQuestionModel> questions = phaseIdToQuestionsMap
                .getOrDefault(phaseId, new ArrayList<>());

            questions.add(question);
            phaseIdToQuestionsMap.put(phaseId, questions);
        }

        for (final TestOptionModel option : optionsList) {
            final int questionId = option.getQuestion().getId();
            final ArrayList<TestOptionModel> options = questionIdToOptionsMap
                .getOrDefault(questionId, new ArrayList<>());

            options.add(option);
            questionIdToOptionsMap.put(questionId, options);
        }

        final AssignedTestGroup group = new AssignedTestGroup(
            groupModel.getLabel(),
            groupModel.getGreeting(),
            groupModel.isAllowPreviousPhase(),
            groupModel.isAllowPreviousQuestion(),
            groupModel.isAllowSkipQuestion(),
            new ArrayList<>()
        );

        for (final TestPhaseModel phaseModel : phasesList) {
            final AssignedTestPhase phase = new AssignedTestPhase(new ArrayList<>());

            group.phases().add(phase);

            final ArrayList<TestQuestionModel> questions = phaseIdToQuestionsMap.get(phaseModel.getId());

            if (phaseModel.isRandomizeQuestions()) {
                Collections.shuffle(questions, new SecureRandom());
            }

            for (final TestQuestionModel questionModel : questions) {
                final Image questionImage = questionModel.getImage() != null
                    ? imageService.getImageById(questionModel.getImage().getId())
                    : null;

                final AssignedTestQuestion question = new AssignedTestQuestion(
                    questionModel.getId(),
                    questionModel.getText(),
                    questionImage,
                    new ArrayList<>()
                );

                phase.questions().add(question);

                final ArrayList<TestOptionModel> options = questionIdToOptionsMap.get(questionModel.getId());

                if (questionModel.isRandomizeOptions()) {
                    Collections.shuffle(options, new SecureRandom());
                }

                for (final TestOptionModel optionModel : options) {
                    final Image optionImage = optionModel.getImage() != null
                        ? imageService.getImageById(optionModel.getImage().getId())
                        : null;

                    final AssignedTestOption option = new AssignedTestOption(
                        optionModel.getId(),
                        optionModel.getText(),
                        optionImage
                    );

                    question.options().add(option);
                }
            }
        }

        return group;
    }

    @Transactional
    public List<TestGroup> getGroupsByConfigVersion(int configVersion) {
        final List<TestGroupModel> groupsList = testGroupRepository.findAllByConfigVersion(configVersion);

        if (groupsList.isEmpty()) {
            return List.of();
        }

        final List<TestPhaseModel> phasesList = testPhaseRepository
            .findAllByGroupConfigVersionOrderByOrder(configVersion);
        final List<TestQuestionModel> questionsList = testQuestionRepository
            .findAllByPhaseGroupConfigVersionOrderByOrder(configVersion);
        final List<TestOptionModel> optionsList = testOptionRepository
            .findAllByQuestionPhaseGroupConfigVersionOrderByOrder(configVersion);

        final HashMap<Integer, TestGroup> groupsMap = new HashMap<>();
        final HashMap<Integer, TestPhase> phasesMap = new HashMap<>();
        final HashMap<Integer, TestQuestion> questionsMap = new HashMap<>();

        for (final TestGroupModel groupModel : groupsList) {
            final TestGroup group = new TestGroup(
                groupModel.getId(),
                groupModel.getProbability(),
                groupModel.getLabel(),
                groupModel.getGreeting(),
                groupModel.isAllowPreviousPhase(),
                groupModel.isAllowPreviousQuestion(),
                groupModel.isAllowSkipQuestion(),
                groupModel.isRandomizePhases(),
                new ArrayList<>()
            );

            groupsMap.put(group.id(), group);
        }

        for (final TestPhaseModel phaseModel : phasesList) {
            final TestGroup group = groupsMap.get(phaseModel.getGroup().getId());
            if (group == null) {
                continue;
            }

            final TestPhase phase = new TestPhase(
                phaseModel.getId(),
                phaseModel.isRandomizeQuestions(),
                new ArrayList<>()
            );

            group.phases().add(phase);
            phasesMap.put(phase.id(), phase);
        }

        for (final TestQuestionModel questionModel : questionsList) {
            final TestPhase phase = phasesMap.get(questionModel.getPhase().getId());
            if (phase == null) {
                continue;
            }

            final Image questionImage = questionModel.getImage() != null
                ? imageService.getImageById(questionModel.getImage().getId())
                : null;

            final TestQuestion question = new TestQuestion(
                questionModel.getId(),
                questionModel.getText(),
                questionImage,
                questionModel.isRandomizeOptions(),
                new ArrayList<>()
            );

            phase.questions().add(question);
            questionsMap.put(question.id(), question);
        }

        for (final TestOptionModel optionModel : optionsList) {
            final TestQuestion question = questionsMap.get(optionModel.getQuestion().getId());
            if (question == null) {
                continue;
            }

            final Image optionImage = optionModel.getImage() != null
                ? imageService.getImageById(optionModel.getImage().getId())
                : null;

            final TestOption option = new TestOption(
                optionModel.getId(),
                optionModel.getText(),
                optionImage,
                optionModel.isCorrect()
            );

            question.options().add(option);
        }

        return groupsMap.values().stream().toList();
    }

    @Transactional
    public void cloneGroups(int originalConfigVersion, ConfigModel config) {
        final List<TestGroupModel> groups = testGroupRepository.findAllByConfigVersion(originalConfigVersion);

        for (final TestGroupModel group : groups) {
            final TestGroupModel newGroup = testGroupRepository.save(TestGroupModel.builder()
                .config(config)
                .label(group.getLabel())
                .probability(group.getProbability())
                .greeting(group.getGreeting())
                .allowPreviousPhase(group.isAllowPreviousPhase())
                .allowPreviousQuestion(group.isAllowPreviousQuestion())
                .allowSkipQuestion(group.isAllowSkipQuestion())
                .randomizePhases(group.isRandomizePhases())
                .build()
            );

            testPhaseService.clonePhases(group.getId(), newGroup);
        }
    }

    @Transactional
    public boolean updateGroups(
        ConfigModel config,
        List<TestGroupUpdate> groupsUpdates,
        List<MultipartFile> imageFiles
    ) {
        final List<TestGroupModel> storedGroups = testGroupRepository.findAllByConfigVersion(config.getVersion());
        final List<TestPhaseModel> storedPhases = testPhaseRepository
            .findAllByGroupConfigVersionOrderByOrder(config.getVersion());
        final List<TestQuestionModel> storedQuestions = testQuestionRepository
            .findAllByPhaseGroupConfigVersionOrderByOrder(config.getVersion());
        final List<TestOptionModel> storedOptions = testOptionRepository
            .findAllByQuestionPhaseGroupConfigVersionOrderByOrder(config.getVersion());

        final AtomicBoolean modified = new AtomicBoolean(false);

        final Map<Integer, TestGroupModel> storedGroupsMap = new HashMap<>();
        final Map<Integer, TestPhaseModel> storedPhasesMap = new HashMap<>();
        final Map<Integer, TestQuestionModel> storedQuestionsMap = new HashMap<>();
        final Map<Integer, TestOptionModel> storedOptionsMap = new HashMap<>();

        final Map<Integer, Boolean> processedGroups = new HashMap<>();
        final Map<Integer, Boolean> processedPhases = new HashMap<>();
        final Map<Integer, Boolean> processedQuestions = new HashMap<>();
        final Map<Integer, Boolean> processedOptions = new HashMap<>();

        for (final TestGroupModel group : storedGroups) {
            storedGroupsMap.put(group.getId(), group);
            processedGroups.put(group.getId(), false);
        }

        for (final TestPhaseModel phase : storedPhases) {
            storedPhasesMap.put(phase.getId(), phase);
            processedPhases.put(phase.getId(), false);
        }

        for (final TestQuestionModel question : storedQuestions) {
            storedQuestionsMap.put(question.getId(), question);
            processedQuestions.put(question.getId(), false);
        }

        for (final TestOptionModel option : storedOptions) {
            storedOptionsMap.put(option.getId(), option);
            processedOptions.put(option.getId(), false);
        }

        final AtomicInteger imageIndex = new AtomicInteger(0);

        for (final TestGroupUpdate groupUpdate : groupsUpdates != null ? groupsUpdates : List.<TestGroupUpdate>of()) {
            final boolean isNew = groupUpdate.id() == null
                || !storedGroupsMap.containsKey(groupUpdate.id());

            if (isNew) {
                final TestGroupModel newGroup = TestGroupModel.builder()
                    .config(config)
                    .label(groupUpdate.label())
                    .probability((byte) groupUpdate.probability())
                    .greeting(groupUpdate.greeting())
                    .allowPreviousPhase(groupUpdate.allowPreviousPhase())
                    .allowPreviousQuestion(groupUpdate.allowPreviousQuestion())
                    .allowSkipQuestion(groupUpdate.allowSkipQuestion())
                    .randomizePhases(groupUpdate.randomizePhases())
                    .build();

                final TestGroupModel savedGroup = testGroupRepository.save(newGroup);

                testPhaseService.updatePhases(
                    savedGroup,
                    groupUpdate.phases(),
                    storedPhasesMap,
                    storedQuestionsMap,
                    storedOptionsMap,
                    processedPhases,
                    processedQuestions,
                    processedOptions,
                    imageIndex,
                    imageFiles
                );

                modified.set(true);
                continue;
            }

            final int groupId = groupUpdate.id();
            final TestGroupModel storedGroup = storedGroupsMap.get(groupId);

            processedGroups.put(groupId, true);

            final boolean updated = !Objects.equals(storedGroup.getLabel(), groupUpdate.label())
                || storedGroup.getProbability() != groupUpdate.probability()
                || !Objects.equals(storedGroup.getGreeting(), groupUpdate.greeting())
                || storedGroup.isAllowPreviousPhase() != groupUpdate.allowPreviousPhase()
                || storedGroup.isAllowPreviousQuestion() != groupUpdate.allowPreviousQuestion()
                || storedGroup.isAllowSkipQuestion() != groupUpdate.allowSkipQuestion()
                || storedGroup.isRandomizePhases() != groupUpdate.randomizePhases();

            if (!updated) {
                final boolean modifiedPhases = testPhaseService.updatePhases(
                    storedGroup,
                    groupUpdate.phases(),
                    storedPhasesMap,
                    storedQuestionsMap,
                    storedOptionsMap,
                    processedPhases,
                    processedQuestions,
                    processedOptions,
                    imageIndex,
                    imageFiles
                );

                modified.set(modifiedPhases || modified.get());
                continue;
            }

            storedGroup.setLabel(groupUpdate.label());
            storedGroup.setProbability((byte) groupUpdate.probability());
            storedGroup.setGreeting(groupUpdate.greeting());
            storedGroup.setAllowPreviousPhase(groupUpdate.allowPreviousPhase());
            storedGroup.setAllowPreviousQuestion(groupUpdate.allowPreviousQuestion());
            storedGroup.setAllowSkipQuestion(groupUpdate.allowSkipQuestion());
            storedGroup.setRandomizePhases(groupUpdate.randomizePhases());

            final TestGroupModel updatedGroup = testGroupRepository.save(storedGroup);

            testPhaseService.updatePhases(
                updatedGroup,
                groupUpdate.phases(),
                storedPhasesMap,
                storedQuestionsMap,
                storedOptionsMap,
                processedPhases,
                processedQuestions,
                processedOptions,
                imageIndex,
                imageFiles
            );

            modified.set(true);
        }

        processedOptions.forEach((optionId, processed) -> {
            if (processed) {
                return;
            }

            if (userTestOptionLogRepository.existsByOptionId(optionId)) {
                throw HttpException.conflict(
                    "Test option with id " + optionId + " has user data related to it and cannot be deleted"
                );
            }

            final TestOptionModel option = storedOptionsMap.get(optionId);
            final ImageModel image = option.getImage();

            testOptionRepository.delete(option);

            if (image != null) {
                imageService.deleteImageIfUnused(image);
            }

            modified.set(true);
        });

        processedQuestions.forEach((questionId, processed) -> {
            if (processed) {
                return;
            }

            if (userTestQuestionLogRepository.existsByQuestionId(questionId)) {
                throw HttpException.conflict(
                    "Test question with id " + questionId + " has user data related to it and cannot be deleted"
                );
            }

            final TestQuestionModel question = storedQuestionsMap.get(questionId);
            final ImageModel image = question.getImage();

            testQuestionRepository.delete(question);

            if (image != null) {
                imageService.deleteImageIfUnused(image);
            }

            modified.set(true);
        });

        processedPhases.forEach((phaseId, processed) -> {
            if (processed) {
                return;
            }

            if (
                userTestOptionLogRepository.existsByOptionQuestionPhaseId(phaseId)
                || userTestQuestionLogRepository.existsByQuestionPhaseId(phaseId)
            ) {
                throw HttpException.conflict(
                    "Test phase with id " + phaseId + " has user data related to it and cannot be deleted"
                );
            }

            final TestPhaseModel phase = storedPhasesMap.get(phaseId);

            testPhaseRepository.delete(phase);
            modified.set(true);
        });

        processedGroups.forEach((groupId, processed) -> {
            if (processed) {
                return;
            }

            if (userTestAttemptRepository.existsByGroupId(groupId)) {
                throw HttpException.conflict(
                    "Test group with id " + groupId + " has user data related to it and cannot be deleted"
                );
            }

            final TestGroupModel group = storedGroupsMap.get(groupId);

            testGroupRepository.delete(group);
            modified.set(true);
        });

        return modified.get();
    }
}
