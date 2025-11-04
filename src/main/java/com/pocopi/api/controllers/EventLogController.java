package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.event.NewOptionEventLog;
import com.pocopi.api.dto.event.NewQuestionEventLog;
import com.pocopi.api.dto.event.QuestionEventLog;
import com.pocopi.api.dto.event.QuestionEventLogWithUserId;
import com.pocopi.api.services.EventLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/event-logs")
@Tag(name = "Event logs")
public class EventLogController {
    private final EventLogService eventLogService;

    public EventLogController(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<QuestionEventLogWithUserId>> getAllEventLogs() {
        final List<QuestionEventLogWithUserId> questionEventLogs = eventLogService.getAllEventLogs();
        return ResponseEntity.ok(questionEventLogs);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<QuestionEventLog>> getUserEventLogs(@PathVariable int userId) {
        final List<QuestionEventLog> questionEventLogs = eventLogService.getEventLogsByUserId(userId);
        return ResponseEntity.ok(questionEventLogs);
    }

    @PostMapping("/question")
    public ResponseEntity<Void> saveQuestionEventLog(
        @RequestBody NewQuestionEventLog questionEventLog,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        eventLogService.saveQuestionEventLog(questionEventLog, authUser.getId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/option")
    public ResponseEntity<Void> saveOptionEventLog(
        @RequestBody NewOptionEventLog optionEventLog,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        eventLogService.saveOptionEventLog(optionEventLog, authUser.getId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
