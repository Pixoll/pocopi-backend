package com.pocopi.api.services;

import com.pocopi.api.dto.test.TestPhaseUpdate;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestOptionModel;
import com.pocopi.api.models.test.TestPhaseModel;
import com.pocopi.api.models.test.TestQuestionModel;
import com.pocopi.api.repositories.TestPhaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TestPhaseService {
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionService testQuestionService;

    public TestPhaseService(TestPhaseRepository testPhaseRepository, TestQuestionService testQuestionService) {
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionService = testQuestionService;
    }

    @Transactional
    public boolean updatePhases(
        TestGroupModel group,
        List<TestPhaseUpdate> phasesUpdates,
        Map<Integer, TestPhaseModel> storedPhasesMap,
        Map<Integer, TestQuestionModel> storedQuestionsMap,
        Map<Integer, TestOptionModel> storedOptionsMap,
        Map<Integer, Boolean> processedPhases,
        Map<Integer, Boolean> processedQuestions,
        Map<Integer, Boolean> processedOptions,
        AtomicInteger imageIndex,
        Map<Integer, MultipartFile> imageFiles
    ) {
        if (phasesUpdates == null || phasesUpdates.isEmpty()) {
            return true;
        }

        boolean modified = false;

        byte order = 0;

        for (final TestPhaseUpdate phaseUpdate : phasesUpdates) {
            final boolean isNew = phaseUpdate.id() == null
                || !storedPhasesMap.containsKey(phaseUpdate.id());

            if (isNew) {
                final TestPhaseModel newPhase = TestPhaseModel.builder()
                    .group(group)
                    .order(order++)
                    .randomizeQuestions(phaseUpdate.randomizeQuestions())
                    .build();

                final TestPhaseModel savedPhase = testPhaseRepository.save(newPhase);

                testQuestionService.updateQuestions(
                    savedPhase,
                    phaseUpdate.questions(),
                    storedQuestionsMap,
                    storedOptionsMap,
                    processedQuestions,
                    processedOptions,
                    imageIndex,
                    imageFiles
                );

                modified = true;
                continue;
            }

            final int phaseId = phaseUpdate.id();
            final TestPhaseModel storedPhase = storedPhasesMap.get(phaseId);

            processedPhases.put(phaseId, true);

            final boolean updated = storedPhase.isRandomizeQuestions() != phaseUpdate.randomizeQuestions()
                || storedPhase.getOrder() != order++;

            if (!updated) {
                final boolean modifiedQuestions = testQuestionService.updateQuestions(
                    storedPhase,
                    phaseUpdate.questions(),
                    storedQuestionsMap,
                    storedOptionsMap,
                    processedQuestions,
                    processedOptions,
                    imageIndex,
                    imageFiles
                );

                modified = modifiedQuestions || modified;
                continue;
            }

            storedPhase.setOrder(order);
            storedPhase.setRandomizeQuestions(phaseUpdate.randomizeQuestions());

            final TestPhaseModel savedPhase = testPhaseRepository.save(storedPhase);

            testQuestionService.updateQuestions(
                savedPhase,
                phaseUpdate.questions(),
                storedQuestionsMap,
                storedOptionsMap,
                processedQuestions,
                processedOptions,
                imageIndex,
                imageFiles
            );

            modified = true;
        }

        return modified;
    }
}
