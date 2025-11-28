package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.attempt.UserTestAttemptSummary;
import com.pocopi.api.dto.attempt.UsersTestAttemptsSummary;
import com.pocopi.api.services.SummaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/summaries")
@Tag(name = "Summaries")
public class SummaryController {
    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UsersTestAttemptsSummary> getAllUsersTestAttemptsSummary() {
        final UsersTestAttemptsSummary summary = summaryService.getAllUsersTestAttemptsSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/me")
    public ResponseEntity<UserTestAttemptSummary> getCurrentUserLatestTestAttemptSummary(
        @AuthenticationPrincipal AuthUser authUser
    ) {
        final UserTestAttemptSummary summary = summaryService.getUserLatestTestAttemptSummary(authUser.getId());
        return ResponseEntity.ok(summary);
    }
}
