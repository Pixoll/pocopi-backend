package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.event.NewOptionEventLog;
import com.pocopi.api.dto.event.NewQuestionEventLog;
import com.pocopi.api.services.EventLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/event-logs")
@Tag(name = "Event logs")
public class EventLogController {
    private final EventLogService eventLogService;

    public EventLogController(EventLogService eventLogService) {
        this.eventLogService = eventLogService;
    }

    @PostMapping("/question")
    public ResponseEntity<Void> saveQuestionEventLog(
        @RequestBody @Valid NewQuestionEventLog questionEventLog,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        eventLogService.saveQuestionEventLog(questionEventLog, authUser.getId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/option")
    public ResponseEntity<Void> saveOptionEventLog(
        @RequestBody @Valid NewOptionEventLog optionEventLog,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        eventLogService.saveOptionEventLog(optionEventLog, authUser.getId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
