package com.pocopi.api.services;

import com.pocopi.api.dto.image.Image;
import com.pocopi.api.dto.test.*;
import com.pocopi.api.models.test.*;
import com.pocopi.api.repositories.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestGroupService {
    private final TestGroupRepository testGroupRepository;
    private final ImageService imageService;
    private final TestProtocolService testProtocolService;
    private final TestProtocolRepository testProtocolRepository;
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;

    public TestGroupService(
        TestGroupRepository testGroupRepository,
        ImageService imageService,
        TestProtocolService testProtocolService,
        TestProtocolRepository testProtocolRepository,
        TestPhaseRepository testPhaseRepository,
        TestQuestionRepository testQuestionRepository,
        TestOptionRepository testOptionRepository
    ) {
        this.testGroupRepository = testGroupRepository;
        this.imageService = imageService;
        this.testProtocolService = testProtocolService;
        this.testProtocolRepository = testProtocolRepository;
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
    }

    public TestGroupModel getTestGroup(int id) {
        return testGroupRepository.findById(id).orElse(null);
    }

    public TestGroupModel sampleGroup() {
        final List<TestGroupModel> groups = testGroupRepository.findAll();

        final SecureRandom random = new SecureRandom();
        final long randomValue = Math.abs(random.nextLong());

        final String randomStr = String.valueOf(randomValue);
        final String reversedStr = new StringBuilder(randomStr).reverse().toString();
        final BigDecimal targetProbability = new BigDecimal("0." + reversedStr);

        final ArrayList<BigDecimal> probabilitySums = new ArrayList<>();
        BigDecimal lastProbability = BigDecimal.ZERO;

        for (final TestGroupModel group : groups) {
            final BigDecimal prob = new BigDecimal(group.getProbability())
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
            lastProbability = lastProbability.add(prob);
            probabilitySums.add(lastProbability);
        }

        int left = 0;
        int right = probabilitySums.size() - 1;
        int index = 0;

        while (left <= right) {
            final int mid = left + (right - left) / 2;
            final BigDecimal value = probabilitySums.get(mid);

            if (value.compareTo(targetProbability) > 0) {
                index = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return groups.get(index);
    }

    @Transactional
    public Map<String, TestGroup> getGroupsByConfigVersion(int configVersion) {
        final Map<String, TestGroupModel> groupModelsMap = testGroupRepository.findAllByConfigVersion(configVersion)
            .stream()
            .collect(Collectors.toMap(TestGroupModel::getLabel, (g) -> g, (a, b) -> b));

        if (groupModelsMap.isEmpty()) {
            return Map.of();
        }

        final List<TestProtocolModel> protocolsList = testProtocolRepository.findAllByConfigVersion(configVersion);
        final List<TestPhaseModel> phasesList = testPhaseRepository.findAllByProtocolConfigVersion(configVersion);
        final List<TestQuestionModel> questionsList = testQuestionRepository
            .findAllByPhaseProtocolConfigVersion(configVersion);
        final List<TestOptionModel> optionsList = testOptionRepository
            .findAllByQuestionPhaseProtocolConfigVersion(configVersion);

        final HashMap<String, TestGroup> groupsMap = new HashMap<>();
        final HashMap<Integer, TestProtocol> protocolsMap = new HashMap<>();
        final HashMap<Integer, TestPhase> phasesMap = new HashMap<>();
        final HashMap<Integer, TestQuestion> questionsMap = new HashMap<>();

        for (final TestProtocolModel protocolModel : protocolsList) {
            final TestGroupModel groupModel = protocolModel.getGroup();
            if (groupModel == null) {
                continue;
            }

            final TestProtocol protocol = new TestProtocol(
                protocolModel.getId(),
                protocolModel.getLabel(),
                protocolModel.isAllowPreviousPhase(),
                protocolModel.isAllowPreviousQuestion(),
                protocolModel.isAllowSkipQuestion(),
                new ArrayList<>()
            );

            final TestGroup group = new TestGroup(
                groupModel.getId(),
                groupModel.getProbability(),
                groupModel.getLabel(),
                groupModel.getGreeting(),
                protocol
            );

            protocolsMap.put(protocol.id(), protocol);
            groupsMap.put(group.label(), group);
        }

        for (final TestPhaseModel phaseModel : phasesList) {
            final TestProtocol protocol = protocolsMap.get(phaseModel.getProtocol().getId());
            if (protocol == null) {
                continue;
            }

            final TestPhase phase = new TestPhase(phaseModel.getId(), new ArrayList<>());

            protocol.phases().add(phase);
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

        return groupsMap;
    }

    public List<TestGroupModel> finAllTestGroups() {
        return testGroupRepository.findAll();
    }

    public TestGroupModel saveTestGroup(TestGroupModel testGroupModel) {
        return testGroupRepository.save(testGroupModel);
    }

    public Map<String, String> processGroups(Map<String, TestGroupUpdate> groups, Map<Integer, File> images) {
        final Map<String, String> results = new HashMap<>();
        final List<TestGroupModel> allExistingGroups = finAllTestGroups();
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
                final TestGroupModel savedGroup = getTestGroup(groupId);

                if (savedGroup == null) {
                    results.put("group_" + groupId, "Group not found");
                    continue;
                }

                final boolean infoChanged = checkChangeByGroup(updatedGroup, savedGroup);

                if (infoChanged) {
                    savedGroup.setGreeting(updatedGroup.greeting());
                    savedGroup.setLabel(updatedGroup.label());
                    savedGroup.setProbability((byte) updatedGroup.probability());

                    saveTestGroup(savedGroup);
                }

                final Map<String, String> protocolResults = testProtocolService.processProtocol(
                    savedGroup,
                    updatedGroup.protocol(),
                    images,
                    imageIndexTracker,
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
                final TestGroupModel newGroup = new TestGroupModel();
                newGroup.setProbability((byte) updatedGroup.probability());
                newGroup.setGreeting(updatedGroup.greeting());
                newGroup.setLabel(updatedGroup.label());

                final TestGroupModel savedGroup = saveTestGroup(newGroup);

                final Map<String, String> protocolResults = testProtocolService.processProtocol(
                    savedGroup,
                    updatedGroup.protocol(),
                    images,
                    imageIndexTracker,
                    results
                );
                results.putAll(protocolResults);

                results.put("group_new_" + groupKey, "Created with ID: " + savedGroup.getId());
            }
        }

        for (final Map.Entry<Integer, Boolean> entry : processedGroups.entrySet()) {
            if (!entry.getValue()) {
                final TestGroupModel groupToDelete = getTestGroup(entry.getKey());
                if (groupToDelete != null) {
                    deleteGroupWithProtocol(groupToDelete);
                    results.put("group_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    public void deleteTestGroup(TestGroupModel testGroupModel) {
        testGroupRepository.delete(testGroupModel);
    }

    private void deleteGroupWithProtocol(TestGroupModel group) {
        final TestProtocolModel protocol = testProtocolService.findByGroup(group);
        deleteTestGroup(group);
        if (protocol != null) {
            testProtocolService.deleteWithPhases(protocol);
        }
    }

    private boolean checkChangeByGroup(TestGroupUpdate updatedGroup, TestGroupModel savedGroup) {
        return !Objects.equals(updatedGroup.label(), savedGroup.getLabel())
               || updatedGroup.probability() != savedGroup.getProbability()
               || !Objects.equals(updatedGroup.greeting(), savedGroup.getGreeting());
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
