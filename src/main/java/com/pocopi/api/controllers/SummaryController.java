package com.pocopi.api.controllers;

import com.pocopi.api.dto.user.UserSummary;
import com.pocopi.api.dto.user.UsersSummary;
import com.pocopi.api.services.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/summary")
public class SummaryController {
    private final SummaryService summaryService;

    @Autowired
    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersSummary> getAllUserSummaries() {
        final UsersSummary response = summaryService.getAllUserSummaries();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // TODO there should be another one to get the current user summary
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserSummary> getUserSummaryById(@PathVariable int userId) {
        final UserSummary response = summaryService.getUserSummaryById(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
