package com.pocopi.api.controllers;

import com.pocopi.api.dto.Form.FormAnswerRequest;
import com.pocopi.api.services.interfaces.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    private final FormService formService;

    @Autowired
    public FormController(FormService formService) {
        this.formService = formService;
    }

    @PostMapping("/answer")
    public ResponseEntity<?> submitFormAnswers(@RequestBody FormAnswerRequest request) {
        try {
            formService.saveUserFormAnswers(request);
            return ResponseEntity.ok().body("Respuestas guardadas");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar las respuestas: " + e.getMessage());
        }
    }
}