package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.TestGroup.PatchPhase;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestProtocolModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.TestPhaseRepository;
import com.pocopi.api.services.interfaces.TestPhaseService;
import com.pocopi.api.services.interfaces.TestQuestionService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestPhaseServiceImp implements TestPhaseService {
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionService testQuestionService;

    public TestPhaseServiceImp(TestPhaseRepository testPhaseRepository,  TestQuestionService testQuestionService) {
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionService = testQuestionService;
    }

    @Override
    public Map<String, String> processPhases(
        TestProtocolModel protocol,
        List<PatchPhase> phases,
        Map<Integer, File> images,
        TestGroupServiceImp.ImageIndexTracker imageIndexTracker
    ) {
        Map<String, String> results = new HashMap<>();
        List<TestPhaseModel> allExistingPhases = testPhaseRepository.findAllByProtocol(protocol);
        Map<Integer, Boolean> processedPhases = new HashMap<>();

        for (TestPhaseModel phase : allExistingPhases) {
            processedPhases.put(phase.getId(), false);
        }

        int order = 0;
        for (PatchPhase patchPhase : phases) {
            if (patchPhase.id().isPresent()) {
                int phaseId = patchPhase.id().get();
                TestPhaseModel savedPhase = testPhaseRepository.findById(phaseId).orElse(null);

                if (savedPhase == null) {
                    results.put("phase_" + phaseId, "Phase not found");
                    order++;
                    continue;
                }

                boolean orderChanged = savedPhase.getOrder() != order;

                if (orderChanged) {
                    savedPhase.setOrder((byte) order);
                    testPhaseRepository.save(savedPhase);
                }

                Map<String, String> questionResults = testQuestionService.processTestQuestions(
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
                TestPhaseModel newPhase = new TestPhaseModel();
                newPhase.setOrder((byte) order);
                newPhase.setProtocol(protocol);

                TestPhaseModel savedPhase = testPhaseRepository.save(newPhase);

                Map<String, String> questionResults = testQuestionService.processTestQuestions(
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

        for (Map.Entry<Integer, Boolean> entry : processedPhases.entrySet()) {
            if (!entry.getValue()) {
                TestPhaseModel phaseToDelete = testPhaseRepository.findById(entry.getKey()).orElse(null);
                if (phaseToDelete != null) {
                    deleteWithQuestions(phaseToDelete);
                    results.put("phase_" + entry.getKey(), "Deleted");
                }
            }
        }

        return results;
    }

    private void deleteWithQuestions(TestPhaseModel phase) {
        List<TestQuestionModel> questions = testQuestionService.findAllByPhase(phase);
        for (TestQuestionModel question : questions) {
            testQuestionService.deleteWithOptions(question);
        }
        testPhaseRepository.deleteById(phase.getId());
    }
}
