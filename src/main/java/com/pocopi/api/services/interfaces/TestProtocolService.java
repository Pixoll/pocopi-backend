package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchProtocol;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestProtocolModel;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestProtocolService {

    Map<String, String> processProtocol(
        TestGroupModel group,
        PatchProtocol updatedProtocol,
        List<Optional<File>> images,
        int imageIndex,
        Map<String, String> results
    );
    TestProtocolModel findByGroup(TestGroupModel group);
    void deleteWithPhases(TestProtocolModel protocol);
}
