package com.pocopi.api.controllers;

import com.pocopi.api.dto.time_log.TimeLog;
import com.pocopi.api.services.TimeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/timelogs")
public class TimeLogController {
    TimeLogService timeLogsService;

    @Autowired
    public TimeLogController(TimeLogService timeLogsService) {
        this.timeLogsService = timeLogsService;
    }

    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs() {
        final List<TimeLog> response = timeLogsService.getTimeLogs();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<TimeLog> getUserTimelogs(@PathVariable int userId) {
        final TimeLog response = timeLogsService.getTimeLogByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
