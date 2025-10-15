package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.FormResult.UserFormWithInfoResultsResponse;
import com.pocopi.api.dto.FormResult.GroupFormResultsResponse;

public interface FormResultsService {
    UserFormWithInfoResultsResponse getUserFormResults(int userId);
    GroupFormResultsResponse getGroupFormResults(int groupId);
}