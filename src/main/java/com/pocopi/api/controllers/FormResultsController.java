package com.pocopi.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.FormResult.UserFormResultsResponse;
import com.pocopi.api.dto.FormResult.GroupFormResultsResponse;
import com.pocopi.api.services.interfaces.FormResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

@RestController
@RequestMapping("/api/form-results")
public class FormResultsController {

    private final FormResultsService formResultsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormResultsController(FormResultsService formResultsService, ObjectMapper objectMapper) {
        this.formResultsService = formResultsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/user/{userId}")
    public UserFormResultsResponse getUserFormResults(@PathVariable int userId) {
        return formResultsService.getUserFormResults(userId);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<byte[]> getGroupFormResultsAsZip(@PathVariable int groupId) {
        try {
            GroupFormResultsResponse groupResults = formResultsService.getGroupFormResults(groupId);

            byte[] jsonBytes = objectMapper.writeValueAsBytes(groupResults);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            byte[] gzipBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group-results-" + groupId + ".json.gz\"");
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
