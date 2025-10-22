package com.pocopi.api.services;

import com.pocopi.api.dto.test.*;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestProtocolModel;
import com.pocopi.api.repositories.TestGroupData;
import com.pocopi.api.repositories.TestGroupRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestGroupService {
    private final TestGroupRepository testGroupRepository;
    private final ImageService imageService;
    private final TestProtocolService testProtocolService;

    @Autowired
    public TestGroupService(
        TestGroupRepository testGroupRepository,
        ImageService imageService,
        TestProtocolService testProtocolService
    ) {
        this.testGroupRepository = testGroupRepository;
        this.imageService = imageService;
        this.testProtocolService = testProtocolService;
    }

    public TestGroupModel getTestGroup(int id) {
        return testGroupRepository.findById(id).orElse(null);
    }

    public Map<String, TestGroup> buildGroupResponses(int configVersion) {
        final List<TestGroupData> rows = testGroupRepository.findAllGroupsDataByConfigVersion(configVersion);

        if (rows.isEmpty()) {
            return Map.of();
        }

        final Map<Integer, List<TestGroupData>> groupsMap = new LinkedHashMap<>();
        for (final TestGroupData row : rows) {
            groupsMap.computeIfAbsent(row.getGroupId(), k -> new ArrayList<>()).add(row);
        }

        return groupsMap.values().stream()
            .map(groupRows -> {
                final TestGroupData first = groupRows.getFirst();

                final Map<Integer, List<TestGroupData>> phasesMap = groupRows.stream().collect(Collectors.groupingBy(
                    TestGroupData::getPhaseId,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

                final List<TestPhase> phases = phasesMap.values().stream()
                    .map(phaseRows -> {
                        final Map<Integer, List<TestGroupData>> questionsMap = phaseRows.stream()
                            .collect(Collectors.groupingBy(
                                TestGroupData::getQuestionOrder,
                                LinkedHashMap::new,
                                Collectors.toList()
                            ));

                        final List<TestQuestion> questions = questionsMap.values().stream()
                            .map(qRows -> {
                                final List<TestOption> options = qRows.stream()
                                    .map(r -> new TestOption(
                                        r.getOptionId(),
                                        r.getOptionText(),
                                        r.getOptionImageId() != null
                                            ? imageService.getImageById(r.getOptionImageId())
                                            : null,
                                        r.getCorrect()
                                    ))
                                    .toList();

                                return new TestQuestion(
                                    qRows.getFirst().getQuestionId(),
                                    qRows.getFirst().getQuestionText(),
                                    imageService.getImageById(qRows.getFirst().getQuestionImageId()),
                                    options
                                );
                            })
                            .toList();

                        return new TestPhase(phaseRows.getFirst().getPhaseId(), questions);
                    })
                    .toList();

                final TestProtocol protocol = new TestProtocol(
                    first.getProtocolId(),
                    first.getProtocolLabel(),
                    first.getAllowPreviousPhase(),
                    first.getAllowPreviousQuestion(),
                    first.getAllowSkipQuestion(),
                    phases
                );

                return Map.entry(
                    first.getGroupLabel(),
                    new TestGroup(
                        first.getGroupId(),
                        first.getProbability(),
                        first.getGroupLabel(),
                        first.getGreeting(),
                        protocol
                    )
                );
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
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
