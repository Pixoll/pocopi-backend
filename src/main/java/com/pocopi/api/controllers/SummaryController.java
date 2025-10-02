package com.pocopi.api.controllers;

import com.pocopi.api.dto.User.TotalUserSummaryResponse;
import com.pocopi.api.dto.User.UserSummaryResponse;
import com.pocopi.api.services.interfaces.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TotalUserSummaryResponse> getAllUserSummaries() {
        TotalUserSummaryResponse response = summaryService.getAllUserSummaries();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSummaryResponse> getUserSummaryById(@PathVariable int userId) {
        UserSummaryResponse response = summaryService.getUserSummaryById(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
