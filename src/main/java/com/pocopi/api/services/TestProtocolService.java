package com.pocopi.api.services;

import com.pocopi.api.dto.test.TestProtocolUpdate;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestProtocolModel;
import com.pocopi.api.repositories.TestProtocolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class TestProtocolService {
    private final TestProtocolRepository testProtocolRepository;
    private final TestPhaseService testPhaseService;

    @Autowired
    public TestProtocolService(TestProtocolRepository testProtocolRepository, TestPhaseService testPhaseService) {
        this.testProtocolRepository = testProtocolRepository;
        this.testPhaseService = testPhaseService;
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
