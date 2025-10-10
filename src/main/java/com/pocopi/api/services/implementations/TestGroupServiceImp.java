package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.TestGroup.*;
import com.pocopi.api.models.TestGroupModel;
import com.pocopi.api.repositories.TestGroupData;
import com.pocopi.api.repositories.TestGroupRepository;
import com.pocopi.api.services.interfaces.ImageService;
import com.pocopi.api.services.interfaces.TestGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestGroupServiceImp implements TestGroupService {

    TestGroupRepository testGroupRepository;
    ImageService imageService;

    @Autowired
    public TestGroupServiceImp(TestGroupRepository testGroupRepository,  ImageService imageService) {
        this.testGroupRepository = testGroupRepository;
        this.imageService = imageService;
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
}
