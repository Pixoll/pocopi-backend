package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.User.TotalUserSummary;
import com.pocopi.api.dto.User.UserSummary;

public interface SummaryService {
    UserSummary getUserSummaryById(int userId);
    TotalUserSummary getAllUserSummaries();
}
