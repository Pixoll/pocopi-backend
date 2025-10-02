package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.User.TotalUserSummaryResponse;
import com.pocopi.api.dto.User.UserSummaryResponse;

public interface SummaryService {
    UserSummaryResponse getUserSummaryById(int userId);
    TotalUserSummaryResponse getAllUserSummaries();
}
