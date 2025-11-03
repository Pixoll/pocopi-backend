package com.pocopi.api.services;

import com.pocopi.api.dto.image.Image;
import com.pocopi.api.dto.test.*;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;

@Service
public class TestGroupService {
    private final TestGroupRepository testGroupRepository;
    private final ConfigRepository configRepository;
    private final TestPhaseService testPhaseService;
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final ImageService imageService;

    public TestGroupService(
        TestGroupRepository testGroupRepository,
        ConfigRepository configRepository,
        TestPhaseService testPhaseService,
        TestPhaseRepository testPhaseRepository,
        TestQuestionRepository testQuestionRepository,
        TestOptionRepository testOptionRepository,
        ImageService imageService
    ) {
        this.testGroupRepository = testGroupRepository;
        this.configRepository = configRepository;
        this.testPhaseService = testPhaseService;
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
        this.imageService = imageService;
    }

    public TestGroupModel sampleGroup() {
        final int configVersion = configRepository.findLastConfig().getVersion();

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

    public Map<String, String> processGroups(Map<String, TestGroupUpdate> groups, Map<Integer, File> images) {
        final Map<String, String> results = new HashMap<>();
        final List<TestGroupModel> allExistingGroups = testGroupRepository.findAll();
        final Map<Integer, Boolean> processedGroups = new HashMap<>();

        for (final TestGroupModel group : allExistingGroups) {
            processedGroups.put(group.getId(), false);
        }

        final ImageIndexTracker imageIndexTracker = new ImageIndexTracker(0);

        for (final Map.Entry<String, TestGroupUpdate> entry : groups.entrySet()) {
            final String groupKey = entry.getKey();
            final TestGroupUpdate updatedGroup = entry.getValue();

            if (updatedGroup.id().isPresent()) {
                final int groupId = updatedGroup.id().get();
                final TestGroupModel savedGroup = testGroupRepository.findById(groupId).orElse(null);

                if (savedGroup == null) {
                    results.put("group_" + groupId, "Group not found");
                    continue;
                }

                final boolean infoChanged = checkChangeByGroup(updatedGroup, savedGroup);

                if (infoChanged) {
                    savedGroup.setGreeting(updatedGroup.greeting());
                    savedGroup.setLabel(updatedGroup.label());
                    savedGroup.setProbability((byte) updatedGroup.probability());
                    savedGroup.setAllowPreviousPhase(updatedGroup.allowPreviousPhase());
                    savedGroup.setAllowPreviousQuestion(updatedGroup.allowPreviousQuestion());
                    savedGroup.setAllowSkipQuestion(updatedGroup.allowSkipQuestion());

                    testGroupRepository.save(savedGroup);
                }

                final Map<String, String> phaseResults = testPhaseService.processPhases(
                    savedGroup,
                    updatedGroup.phases(),
                    images,
                    imageIndexTracker
                );
                results.putAll(phaseResults);

                if (infoChanged) {
                    results.put("group_" + groupId, "Updated successfully");
                } else {
                    results.put("group_" + groupId, "No changes");
                }

                processedGroups.put(groupId, true);
            } else {
                final TestGroupModel newGroup = new TestGroupModel();
                newGroup.setProbability((byte) updatedGroup.probability());
                newGroup.setGreeting(updatedGroup.greeting());
                newGroup.setLabel(updatedGroup.label());
                newGroup.setAllowPreviousPhase(updatedGroup.allowPreviousPhase());
                newGroup.setAllowPreviousQuestion(updatedGroup.allowPreviousQuestion());
                newGroup.setAllowSkipQuestion(updatedGroup.allowSkipQuestion());

                final TestGroupModel savedGroup = testGroupRepository.save(newGroup);

                final Map<String, String> phaseResults = testPhaseService.processPhases(
                    savedGroup,
                    updatedGroup.phases(),
                    images,
                    imageIndexTracker
                );
                results.putAll(phaseResults);

                results.put("group_new_" + groupKey, "Created with ID: " + savedGroup.getId());
            }
        }

        for (final Map.Entry<Integer, Boolean> entry : processedGroups.entrySet()) {
            if (!entry.getValue()) {
                final TestGroupModel groupToDelete = testGroupRepository.findById(entry.getKey()).orElse(null);
                if (groupToDelete != null) {
                    testGroupRepository.delete(groupToDelete);
                    results.put("group_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private boolean checkChangeByGroup(TestGroupUpdate updatedGroup, TestGroupModel savedGroup) {
        return !Objects.equals(updatedGroup.label(), savedGroup.getLabel())
               || updatedGroup.probability() != savedGroup.getProbability()
               || !Objects.equals(updatedGroup.greeting(), savedGroup.getGreeting())
               || updatedGroup.allowPreviousPhase() != savedGroup.isAllowPreviousPhase()
               || updatedGroup.allowPreviousQuestion() != savedGroup.isAllowPreviousQuestion()
               || updatedGroup.allowSkipQuestion() != savedGroup.isAllowSkipQuestion();
    }

    @Setter
    @Getter
    public static class ImageIndexTracker {
        private int index;

        public ImageIndexTracker(int initialIndex) {
            this.index = initialIndex;
        }

        public void increment() {
            index++;
        }
    }
}
