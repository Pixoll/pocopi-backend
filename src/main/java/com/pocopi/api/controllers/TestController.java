package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.attempt.UserTestAttempt;
import com.pocopi.api.services.UserTestAttemptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test")
public class TestController {
    private final UserTestAttemptService userTestAttemptService;

    public TestController(UserTestAttemptService userTestAttemptService) {
        this.userTestAttemptService = userTestAttemptService;
    }

    @PostMapping("/check-active")
    public ResponseEntity<Void> hasActiveTest(@AuthenticationPrincipal AuthUser authUser) {
        userTestAttemptService.assertActiveAttempt(authUser.user());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/begin")
    public ResponseEntity<UserTestAttempt> beginTest(@AuthenticationPrincipal AuthUser authUser) {
        final UserTestAttempt attempt = userTestAttemptService.beginAttempt(authUser.user());
        return new ResponseEntity<>(attempt, HttpStatus.CREATED);
    }

    @PostMapping("/continue")
    public ResponseEntity<UserTestAttempt> continueTest(@AuthenticationPrincipal AuthUser authUser) {
        final UserTestAttempt attempt = userTestAttemptService.continueAttempt(authUser.getId());
        return ResponseEntity.ok(attempt);
    }

    @PostMapping("/discard")
    public ResponseEntity<Void> discardTest(@AuthenticationPrincipal AuthUser authUser) {
        userTestAttemptService.discardAttempt(authUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/end")
    public ResponseEntity<Void> endTest(@AuthenticationPrincipal AuthUser authUser) {
        userTestAttemptService.endAttempt(authUser.getId());
        return ResponseEntity.ok().build();
    }
}
