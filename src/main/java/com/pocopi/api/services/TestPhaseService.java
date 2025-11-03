package com.pocopi.api.services;

import com.pocopi.api.dto.test.TestPhaseUpdate;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.TestPhaseRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestPhaseService {
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionService testQuestionService;

    public TestPhaseService(TestPhaseRepository testPhaseRepository, TestQuestionService testQuestionService) {
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionService = testQuestionService;
    }

    public Map<String, String> processPhases(
        TestGroupModel group,
        List<TestPhaseUpdate> phases,
        Map<Integer, File> images,
        TestGroupService.ImageIndexTracker imageIndexTracker
    ) {
        final Map<String, String> results = new HashMap<>();
        final List<TestPhaseModel> allExistingPhases = testPhaseRepository.findAllByGroupId(group.getId());
        final Map<Integer, Boolean> processedPhases = new HashMap<>();

        for (final TestPhaseModel phase : allExistingPhases) {
            processedPhases.put(phase.getId(), false);
        }

        int order = 0;
        for (final TestPhaseUpdate patchPhase : phases) {
            if (patchPhase.id().isPresent()) {
                final int phaseId = patchPhase.id().get();
                final TestPhaseModel savedPhase = testPhaseRepository.findById(phaseId).orElse(null);

                if (savedPhase == null) {
                    results.put("phase_" + phaseId, "Phase not found");
                    order++;
                    continue;
                }

                final boolean orderChanged = savedPhase.getOrder() != order;

                if (orderChanged) {
                    savedPhase.setOrder((byte) order);
                    testPhaseRepository.save(savedPhase);
                }

                final Map<String, String> questionResults = testQuestionService.processTestQuestions(
                    savedPhase,
                    patchPhase.questions(),
                    images,
                    imageIndexTracker
                );
                results.putAll(questionResults);

                if (orderChanged) {
                    results.put("phase_" + phaseId, "Updated successfully");
                } else {
                    results.put("phase_" + phaseId, "No changes");
                }

                processedPhases.put(phaseId, true);
            } else {
                final TestPhaseModel newPhase = new TestPhaseModel();
                newPhase.setOrder((byte) order);
                newPhase.setGroup(group);

                final TestPhaseModel savedPhase = testPhaseRepository.save(newPhase);

                final Map<String, String> questionResults = testQuestionService.processTestQuestions(
                    savedPhase,
                    patchPhase.questions(),
                    images,
                    imageIndexTracker
                );
                results.putAll(questionResults);

                results.put("phase_new_" + order, "Created with ID: " + savedPhase.getId());
            }

            order++;
        }

        for (final Map.Entry<Integer, Boolean> entry : processedPhases.entrySet()) {
            if (!entry.getValue()) {
                final TestPhaseModel phaseToDelete = testPhaseRepository.findById(entry.getKey()).orElse(null);
                if (phaseToDelete != null) {
                    deleteWithQuestions(phaseToDelete);
                    results.put("phase_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private void deleteWithQuestions(TestPhaseModel phase) {
        final List<TestQuestionModel> questions = testQuestionService.findAllByPhase(phase);
        for (final TestQuestionModel question : questions) {
            testQuestionService.deleteWithOptions(question);
        }
        testPhaseRepository.deleteById(phase.getId());
    }
}
