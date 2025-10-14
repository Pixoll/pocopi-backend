package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.TestGroup.*;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestProtocolModel;
import com.pocopi.api.repositories.TestGroupData;
import com.pocopi.api.repositories.TestGroupRepository;
import com.pocopi.api.services.interfaces.ImageService;
import com.pocopi.api.services.interfaces.TestGroupService;
import com.pocopi.api.services.interfaces.TestProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestGroupServiceImp implements TestGroupService {

    TestGroupRepository testGroupRepository;
    ImageService imageService;
    TestProtocolService testProtocolService;

    @Autowired
    public TestGroupServiceImp(TestGroupRepository testGroupRepository,  ImageService imageService,  TestProtocolService testProtocolService) {
        this.testGroupRepository = testGroupRepository;
        this.imageService = imageService;
        this.testProtocolService = testProtocolService;
    }
    
    @Override
    public TestGroupModel getTestGroup(int id) {
        return testGroupRepository.findById(id).orElse(null);
    }

    @Override
    public Map<String, Group> buildGroupResponses(int configVersion) {
        List<TestGroupData> rows = testGroupRepository.findAllGroupsDataByConfigVersion(configVersion);

        if (rows.isEmpty()) {
            return Map.of();
        }

        Map<Integer, List<TestGroupData>> groupsMap = new LinkedHashMap<>();
        for (TestGroupData row : rows) {
            groupsMap.computeIfAbsent(row.getGroupId(), k -> new ArrayList<>()).add(row);
        }


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

                Protocol protocol = new Protocol(
                    first.getProtocolId(),
                    first.getProtocolLabel(),
                    first.getAllowPreviousPhase(),
                    first.getAllowPreviousQuestion(),
                    first.getAllowSkipQuestion(),
                    phases
                );
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

    @Override
    public List<TestGroupModel> finAllTestGroups() {
        return testGroupRepository.findAll();
    }

    @Override
    public TestGroupModel saveTestGroup(TestGroupModel testGroupModel) {
        return testGroupRepository.save(testGroupModel);
    }
    @Override
    public Map<String, String> processGroups(Map<String, PatchGroup> groups, List<File> images) {
        Map<String, String> results = new HashMap<>();
        List<TestGroupModel> allExistingGroups = finAllTestGroups();
        Map<Integer, Boolean> processedGroups = new HashMap<>();

        for (TestGroupModel group : allExistingGroups) {
            processedGroups.put(group.getId(), false);
        }

        int imageIndex = 0;

        for (Map.Entry<String, PatchGroup> entry : groups.entrySet()) {
            String groupKey = entry.getKey();
            PatchGroup updatedGroup = entry.getValue();

            if (updatedGroup.id().isPresent()) {
                int groupId = updatedGroup.id().get();
                TestGroupModel savedGroup = getTestGroup(groupId);

                if (savedGroup == null) {
                    results.put("group_" + groupId, "Group not found");
                    continue;
                }

                boolean infoChanged = checkChangeByGroup(updatedGroup, savedGroup);

                if (infoChanged) {
                    savedGroup.setGreeting(updatedGroup.greeting());
                    savedGroup.setLabel(updatedGroup.label());
                    savedGroup.setProbability((byte) updatedGroup.probability());

                    saveTestGroup(savedGroup);
                }

                Map<String, String> protocolResults = testProtocolService.processProtocol(
                    savedGroup,
                    updatedGroup.protocol(),
                    images,
                    imageIndex,
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
                TestGroupModel newGroup = new TestGroupModel();
                newGroup.setProbability((byte) updatedGroup.probability());
                newGroup.setGreeting(updatedGroup.greeting());
                newGroup.setLabel(updatedGroup.label());

                TestGroupModel savedGroup = saveTestGroup(newGroup);

                Map<String, String> protocolResults = testProtocolService.processProtocol(
                    savedGroup,
                    updatedGroup.protocol(),
                    images,
                    imageIndex,
                    results
                );
                results.putAll(protocolResults);

                results.put("group_new_" + groupKey, "Created with ID: " + savedGroup.getId());
            }
        }

        for (Map.Entry<Integer, Boolean> entry : processedGroups.entrySet()) {
            if (!entry.getValue()) {
                TestGroupModel groupToDelete = getTestGroup(entry.getKey());
                if (groupToDelete != null) {
                    deleteGroupWithProtocol(groupToDelete);
                    results.put("group_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private void deleteGroupWithProtocol(TestGroupModel group) {
        TestProtocolModel protocol = testProtocolService.findByGroup(group);
        deleteTestGroup(group);
        if (protocol != null) {
            testProtocolService.deleteWithPhases(protocol);
        }
    }

    private boolean checkChangeByGroup(PatchGroup updatedGroup, TestGroupModel savedGroup) {
        return (
            !Objects.equals(updatedGroup.label(), savedGroup.getLabel()) ||
                updatedGroup.probability() != savedGroup.getProbability() ||
                !Objects.equals(updatedGroup.greeting(), savedGroup.getGreeting())
        );
    }

    @Override
    public void deleteTestGroup(TestGroupModel testGroupModel) {
        testGroupRepository.delete(testGroupModel);
    }
}
