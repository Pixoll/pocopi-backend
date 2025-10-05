package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.TimeLog.TimeLog;

import java.util.List;

public interface TimeLogsService {
    List<TimeLog> getTimeLogs();
    TimeLog getTimeLogByUserId(int userId);
}
