package com.pocopi.api.services;

import com.pocopi.api.dto.image.Image;
import com.pocopi.api.dto.test.*;
import com.pocopi.api.models.test.*;
import com.pocopi.api.repositories.TestOptionRepository;
import com.pocopi.api.repositories.TestPhaseRepository;
import com.pocopi.api.repositories.TestProtocolRepository;
import com.pocopi.api.repositories.TestQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

@Service
public class TestProtocolService {
    private final TestProtocolRepository testProtocolRepository;
    private final TestPhaseService testPhaseService;
    private final TestPhaseRepository testPhaseRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final ImageService imageService;

    public TestProtocolService(
        TestProtocolRepository testProtocolRepository,
        TestPhaseService testPhaseService,
        TestPhaseRepository testPhaseRepository,
        TestQuestionRepository testQuestionRepository,
        TestOptionRepository testOptionRepository,
        ImageService imageService
    ) {
        this.testProtocolRepository = testProtocolRepository;
        this.testPhaseService = testPhaseService;
        this.testPhaseRepository = testPhaseRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
        this.imageService = imageService;
    }

    @Transactional
    public List<TestProtocol> getProtocolsByConfigVersion(int configVersion) {
        final List<TestProtocolModel> protocolsList = testProtocolRepository.findAllByConfigVersion(configVersion);

        if (protocolsList.isEmpty()) {
            return List.of();
        }

        final List<TestPhaseModel> phasesList = testPhaseRepository
            .findAllByProtocolConfigVersionOrderByOrder(configVersion);
        final List<TestQuestionModel> questionsList = testQuestionRepository
            .findAllByPhaseProtocolConfigVersionOrderByOrder(configVersion);
        final List<TestOptionModel> optionsList = testOptionRepository
            .findAllByQuestionPhaseProtocolConfigVersionOrderByOrder(configVersion);

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
                protocolModel.getGroup().getId(),
                protocolModel.getLabel(),
                protocolModel.isAllowPreviousPhase(),
                protocolModel.isAllowPreviousQuestion(),
                protocolModel.isAllowSkipQuestion(),
                protocolModel.isRandomizePhases(),
                new ArrayList<>()
            );

            protocolsMap.put(protocol.id(), protocol);
        }

        for (final TestPhaseModel phaseModel : phasesList) {
            final TestProtocol protocol = protocolsMap.get(phaseModel.getProtocol().getId());
            if (protocol == null) {
                continue;
            }

            final TestPhase phase = new TestPhase(
                phaseModel.getId(),
                phaseModel.isRandomizeQuestions(),
                new ArrayList<>()
            );

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

        return protocolsMap.values().stream().toList();
    }

    public Map<String, String> processProtocol(
        TestGroupModel group,
        TestProtocolUpdate updatedProtocol,
        Map<Integer, File> images,
        TestGroupService.ImageIndexTracker imageIndexTracker,
        Map<String, String> results
    ) {
        final Map<String, String> protocolResults = new HashMap<>();

        if (updatedProtocol.id().isPresent()) {
            final int protocolId = updatedProtocol.id().get();
            final TestProtocolModel savedProtocol = testProtocolRepository.getById(protocolId);

            if (savedProtocol == null) {
                protocolResults.put("protocol_" + protocolId, "Protocol not found");
                return protocolResults;
            }

            final boolean isChanged = checkProtocolChanged(updatedProtocol, savedProtocol);

            if (isChanged) {
                savedProtocol.setLabel(updatedProtocol.label());
                savedProtocol.setAllowPreviousPhase(updatedProtocol.allowPreviousPhase());
                savedProtocol.setAllowPreviousQuestion(updatedProtocol.allowPreviousQuestion());
                savedProtocol.setAllowSkipQuestion(updatedProtocol.allowSkipQuestion());
                testProtocolRepository.save(savedProtocol);
            }

            final Map<String, String> phaseResults = testPhaseService.processPhases(
                savedProtocol,
                updatedProtocol.phases(),
                images,
                imageIndexTracker
            );
            protocolResults.putAll(phaseResults);

            if (isChanged) {
                protocolResults.put("protocol_" + protocolId, "Updated successfully");
            } else {
                protocolResults.put("protocol_" + protocolId, "No changes");
            }
        } else {
            final TestProtocolModel newProtocol = new TestProtocolModel();
            newProtocol.setAllowPreviousPhase(updatedProtocol.allowPreviousPhase());
            newProtocol.setAllowPreviousQuestion(updatedProtocol.allowPreviousQuestion());
            newProtocol.setAllowSkipQuestion(updatedProtocol.allowSkipQuestion());
            newProtocol.setLabel(updatedProtocol.label());
            newProtocol.setGroup(group);

            final TestProtocolModel savedProtocol = testProtocolRepository.save(newProtocol);

            final Map<String, String> phaseResults = testPhaseService.processPhases(
                savedProtocol,
                updatedProtocol.phases(),
                images,
                imageIndexTracker
            );
            protocolResults.putAll(phaseResults);

            protocolResults.put("protocol_new", "Created with ID: " + savedProtocol.getId());
        }

        return protocolResults;
    }

    public TestProtocolModel findByGroup(TestGroupModel group) {
        return testProtocolRepository.findByGroup(group);
    }

    public void deleteWithPhases(TestProtocolModel protocol) {
        testProtocolRepository.deleteById(protocol.getId());
    }

    private boolean checkProtocolChanged(TestProtocolUpdate protocol, TestProtocolModel savedProtocol) {
        return protocol.allowPreviousPhase() != savedProtocol.isAllowPreviousPhase()
               || protocol.allowPreviousQuestion() != savedProtocol.isAllowPreviousQuestion()
               || protocol.allowSkipQuestion() != savedProtocol.isAllowSkipQuestion()
               || !Objects.equals(protocol.label(), savedProtocol.getLabel());
    }
}
