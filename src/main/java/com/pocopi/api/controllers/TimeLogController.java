package com.pocopi.api.controllers;

import com.pocopi.api.dto.TimeLog.SingleTimeLogResponse;
import com.pocopi.api.services.interfaces.TimeLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<List<SingleTimeLogResponse>> getAllTimeLogs(){
        List<SingleTimeLogResponse> response = timeLogsService.getTimeLogs();
        return ResponseEntity.ok(response);
    }
}
