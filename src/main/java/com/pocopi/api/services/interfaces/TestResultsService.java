package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TestResult.UserTestResultsWithInfoResponse;
import com.pocopi.api.dto.TestResult.GroupTestResultsResponse;

public interface TestResultsService {
    UserTestResultsWithInfoResponse getUserTestResults(int userId);
    GroupTestResultsResponse getGroupTestResults(int groupId);
}