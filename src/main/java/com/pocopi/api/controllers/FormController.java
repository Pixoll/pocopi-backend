package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.services.FormService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forms")
@Tag(name = "Forms")
public class FormController {
    private final FormService formService;

    public FormController(FormService formService) {
        this.formService = formService;
    }

    @PostMapping("/answers")
    public ResponseEntity<Void> submitFormAnswers(
        @RequestBody NewFormAnswers formAnswers,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        formService.saveUserFormAnswers(authUser.user(), formAnswers);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
