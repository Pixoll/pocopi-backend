package com.pocopi.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.TestResult.UserTestResultsResponse;
import com.pocopi.api.dto.TestResult.GroupTestResultsResponse;
import com.pocopi.api.services.interfaces.TestResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

@RestController
@RequestMapping("/api/results/test")
public class TestResultsController {

    private final TestResultsService testResultsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TestResultsController(TestResultsService testResultsService, ObjectMapper objectMapper) {
        this.testResultsService = testResultsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/user/{userId}")
    public UserTestResultsResponse getUserTestResults(@PathVariable int userId) {
        return testResultsService.getUserTestResults(userId);
    }

    @GetMapping("/group/{groupId}/zip")
    public ResponseEntity<byte[]> getGroupTestResultsZip(@PathVariable int groupId) {
        try {
            GroupTestResultsResponse groupResults = testResultsService.getGroupTestResults(groupId);

            // Serializa a JSON
            byte[] jsonBytes = objectMapper.writeValueAsBytes(groupResults);

            // Comprime con GZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            byte[] gzipBytes = baos.toByteArray();

            // Headers para descarga gzip
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test-group-results-" + groupId + ".json.gz\"");
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
