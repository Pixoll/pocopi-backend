package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TimeLog.SingleTimeLogResponse;

import java.util.List;

public interface TimeLogsService {
    List<SingleTimeLogResponse> getTimeLogs();
}
