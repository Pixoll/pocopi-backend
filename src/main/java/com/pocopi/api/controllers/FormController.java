package com.pocopi.api.controllers;

import com.pocopi.api.config.auth.AuthUser;
import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.models.form.FormType;
import com.pocopi.api.services.FormAnswerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forms")
@Tag(name = "Forms")
public class FormController {
    private final FormAnswerService formAnswerService;

    public FormController(FormAnswerService formAnswerService) {
        this.formAnswerService = formAnswerService;
    }

    @PostMapping("/{formType}/answers")
    public ResponseEntity<Void> submitFormAnswers(
        @PathVariable FormType formType,
        @RequestBody @Valid NewFormAnswers formAnswers,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        formAnswerService.saveUserFormAnswers(authUser.getId(), formType, formAnswers);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
