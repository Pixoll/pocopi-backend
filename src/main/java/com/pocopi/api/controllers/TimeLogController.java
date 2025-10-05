package com.pocopi.api.controllers;

import com.pocopi.api.dto.TimeLog.TimeLog;
import com.pocopi.api.services.interfaces.TimeLogsService;
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
    TimeLogsService timeLogsService;

    @Autowired
    public TimeLogController(TimeLogsService timeLogsService) {
        this.timeLogsService = timeLogsService;
    }

    @GetMapping
    public ResponseEntity<List<TimeLog>> getAllTimeLogs(){
        List<TimeLog> response = timeLogsService.getTimeLogs();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<TimeLog> getUserTimelogs(@PathVariable int userId){
        TimeLog response = timeLogsService.getTimeLogByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
