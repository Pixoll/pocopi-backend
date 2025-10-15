package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.PatchProtocol;
import com.pocopi.api.models.test.TestGroupModel;
import com.pocopi.api.models.test.TestProtocolModel;
import com.pocopi.api.services.implementations.TestGroupServiceImp;

import java.io.File;
import java.util.Map;

public interface TestProtocolService {

    Map<String, String> processProtocol(
        TestGroupModel group,
        PatchProtocol updatedProtocol,
        Map<Integer, File> images,
        TestGroupServiceImp.ImageIndexTracker imageIndexTracker,
        Map<String, String> results
    );
    TestProtocolModel findByGroup(TestGroupModel group);
    void deleteWithPhases(TestProtocolModel protocol);
}
