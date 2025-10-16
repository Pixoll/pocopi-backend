package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Results.UserAllResultsResponse;
import com.pocopi.api.dto.Results.GroupFullResultsResponse;

public interface ResultsService {
    UserAllResultsResponse getUserAllResults(int userId);
    GroupFullResultsResponse getGroupFullResults(int groupId);
}