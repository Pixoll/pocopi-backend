package com.pocopi.api.controllers;

import com.pocopi.api.dto.form.NewFormAnswers;
import com.pocopi.api.services.FormService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forms")
@Tag(name = "Forms")
public class FormController {
    private final FormService formService;

    @Autowired
    public FormController(FormService formService) {
        this.formService = formService;
    }

    @PostMapping("/answer")
    public ResponseEntity<?> submitFormAnswers(@RequestBody NewFormAnswers request) {
        try {
            formService.saveUserFormAnswers(request);
            return ResponseEntity.ok().body("Respuestas guardadas");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar las respuestas: " + e.getMessage());
        }
    }
}
