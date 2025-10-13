package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestResult.UserTestResultsResponse;
import com.pocopi.api.dto.TestResult.GroupTestResultsResponse;

public interface TestResultsService {
    UserTestResultsResponse getUserTestResults(int userId);
    GroupTestResultsResponse getGroupTestResults(int groupId);
}