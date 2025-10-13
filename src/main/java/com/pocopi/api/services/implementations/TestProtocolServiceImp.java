package com.pocopi.api.services.implementations;

import com.pocopi.api.dto.TestGroup.PatchProtocol;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestProtocolModel;
import com.pocopi.api.repositories.TestProtocolRepository;
import com.pocopi.api.services.interfaces.TestPhaseService;
import com.pocopi.api.services.interfaces.TestProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TestProtocolServiceImp implements TestProtocolService {
    TestProtocolRepository testProtocolRepository;
    TestPhaseService testPhaseService;

    @Autowired
    public TestProtocolServiceImp(TestProtocolRepository testProtocolRepository,  TestPhaseService testPhaseService) {
        this.testProtocolRepository = testProtocolRepository;
        this.testPhaseService = testPhaseService;
    }

    @Override
    public Map<String, String> processProtocol(
        TestGroupModel group,
        PatchProtocol updatedProtocol,
        List<File> images,
        int imageIndex,
        Map<String, String> results
    ) {
        Map<String, String> protocolResults = new HashMap<>();

        if (updatedProtocol.id().isPresent()) {
            int protocolId = updatedProtocol.id().get();
            TestProtocolModel savedProtocol = testProtocolRepository.getById(protocolId);

            if (savedProtocol == null) {
                protocolResults.put("protocol_" + protocolId, "Protocol not found");
                return protocolResults;
            }

            boolean isChanged = checkProtocolChanged(updatedProtocol, savedProtocol);

            if (isChanged) {
                savedProtocol.setLabel(updatedProtocol.label());
                savedProtocol.setAllowPreviousPhase(updatedProtocol.allowPreviousPhase());
                savedProtocol.setAllowPreviousQuestion(updatedProtocol.allowPreviousQuestion());
                savedProtocol.setAllowSkipQuestion(updatedProtocol.allowSkipQuestion());
                testProtocolRepository.save(savedProtocol);
            }

            Map<String, String> phaseResults = testPhaseService.processPhases(
                savedProtocol,
                updatedProtocol.phases(),
                images,
                imageIndex
            );
            protocolResults.putAll(phaseResults);

            if (isChanged) {
                protocolResults.put("protocol_" + protocolId, "Updated successfully");
            } else {
                protocolResults.put("protocol_" + protocolId, "No changes");
            }

        } else {
            TestProtocolModel newProtocol = new TestProtocolModel();
            newProtocol.setAllowPreviousPhase(updatedProtocol.allowPreviousPhase());
            newProtocol.setAllowPreviousQuestion(updatedProtocol.allowPreviousQuestion());
            newProtocol.setAllowSkipQuestion(updatedProtocol.allowSkipQuestion());
            newProtocol.setLabel(updatedProtocol.label());
            newProtocol.setGroup(group);

            TestProtocolModel savedProtocol = testProtocolRepository.save(newProtocol);

            Map<String, String> phaseResults = testPhaseService.processPhases(
                savedProtocol,
                updatedProtocol.phases(),
                images,
                imageIndex
            );
            protocolResults.putAll(phaseResults);

            protocolResults.put("protocol_new", "Created with ID: " + savedProtocol.getId());
        }

        return protocolResults;
    }



    private boolean checkProtocolChanged(PatchProtocol protocol, TestProtocolModel savedProtocol) {
        return (
            protocol.allowPreviousPhase() != savedProtocol.isAllowPreviousPhase() ||
                protocol.allowPreviousQuestion() != savedProtocol.isAllowPreviousQuestion() ||
                protocol.allowSkipQuestion() != savedProtocol.isAllowSkipQuestion() ||
                !Objects.equals(protocol.label(), savedProtocol.getLabel())
        );
    }
    @Override
    public TestProtocolModel findByGroup(TestGroupModel group){
        return testProtocolRepository.findByGroup(group);
    }

    @Override
    public void deleteWithPhases(TestProtocolModel protocol){
        testProtocolRepository.deleteById(protocol.getId());
    }
}
