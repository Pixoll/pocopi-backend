package com.pocopi.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.FormResult.UserFormWithInfoResultsResponse;
import com.pocopi.api.dto.FormResult.GroupFormResultsResponse;
import com.pocopi.api.dto.TestResult.UserTestResultsWithInfoResponse;
import com.pocopi.api.dto.TestResult.GroupTestResultsResponse;
import com.pocopi.api.dto.Results.UserFullResultsResponse;
import com.pocopi.api.dto.Results.GroupFullResultsResponse;
import com.pocopi.api.services.interfaces.FormResultsService;
import com.pocopi.api.services.interfaces.TestResultsService;
import com.pocopi.api.services.interfaces.ResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

@RestController
@RequestMapping("/api/results")
public class ResultsController {

    private final FormResultsService formResultsService;
    private final TestResultsService testResultsService;
    private final ResultsService resultsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ResultsController(
            FormResultsService formResultsService,
            TestResultsService testResultsService,
            ResultsService resultsService,
            ObjectMapper objectMapper
    ) {
        this.formResultsService = formResultsService;
        this.testResultsService = testResultsService;
        this.resultsService = resultsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/user/{userId}/forms")
    public UserFormWithInfoResultsResponse getUserFormResults(@PathVariable int userId) {
        return formResultsService.getUserFormResults(userId);
    }

    @GetMapping("/user/{userId}/tests")
    public UserTestResultsWithInfoResponse getUserTestResults(@PathVariable int userId) {
        return testResultsService.getUserTestResults(userId);
    }

    @GetMapping("/user/{userId}/all")
    public UserFullResultsResponse getUserFullResults(@PathVariable int userId) {
        return resultsService.getUserFullResults(userId);
    }

    @GetMapping("/group/{groupId}/forms")
    public ResponseEntity<byte[]> getGroupFormResultsZip(@PathVariable int groupId) {
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
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group-form-results-" + groupId + ".json.gz\"");
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/group/{groupId}/tests")
    public ResponseEntity<byte[]> getGroupTestResultsZip(@PathVariable int groupId) {
        try {
            GroupTestResultsResponse groupResults = testResultsService.getGroupTestResults(groupId);
            byte[] jsonBytes = objectMapper.writeValueAsBytes(groupResults);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            byte[] gzipBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group-test-results-" + groupId + ".json.gz\"");
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/group/{groupId}/all")
    public ResponseEntity<byte[]> getGroupFullResultsZip(@PathVariable int groupId) {
        try {
            GroupFullResultsResponse groupResults = resultsService.getGroupFullResults(groupId);
            byte[] jsonBytes = objectMapper.writeValueAsBytes(groupResults);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            byte[] gzipBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group-full-results-" + groupId + ".json.gz\"");
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}