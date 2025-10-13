package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestGroup.Group;
import com.pocopi.api.dto.TestGroup.PatchGroup;
import com.pocopi.api.models.TestGroupModel;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface TestGroupService {
    TestGroupModel getTestGroup(int id);
    Map<String, Group> buildGroupResponses(int configVersion);
    List<TestGroupModel> finAllTestGroups();
    TestGroupModel saveTestGroup(TestGroupModel testGroupModel);
    Map<String, String> processGroups(Map<String, PatchGroup> groups, List<File> images);
    void deleteTestGroup(TestGroupModel testGroupModel);
}
