package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Results.UserFullResultsResponse;
import com.pocopi.api.dto.Results.GroupFullResultsResponse;

public interface ResultsService {
    UserFullResultsResponse getUserFullResults(int userId);
    GroupFullResultsResponse getGroupFullResults(int groupId);
}