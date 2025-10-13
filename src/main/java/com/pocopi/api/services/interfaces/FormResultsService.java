package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.FormResult.UserFormResultsResponse;
import com.pocopi.api.dto.FormResult.GroupFormResultsResponse;

public interface FormResultsService {
    UserFormResultsResponse getUserFormResults(int userId);
    GroupFormResultsResponse getGroupFormResults(int groupId);
}