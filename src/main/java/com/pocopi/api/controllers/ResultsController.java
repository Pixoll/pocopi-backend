package com.pocopi.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.results.*;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.FormResultsService;
import com.pocopi.api.services.ResultsService;
import com.pocopi.api.services.TestResultsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@RestController
@RequestMapping("/api/results")
@Tag(name = "Results")
public class ResultsController {
    private final FormResultsService formResultsService;
    private final TestResultsService testResultsService;
    private final ResultsService resultsService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public ResultsController(
        FormResultsService formResultsService,
        TestResultsService testResultsService,
        ResultsService resultsService,
        ObjectMapper objectMapper,
        UserRepository userRepository
    ) {
        this.formResultsService = formResultsService;
        this.testResultsService = testResultsService;
        this.resultsService = resultsService;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}/forms")
    @PreAuthorize("hasAuthority('ADMIN')")
    public FormAnswersByUser getUserFormResults(@PathVariable int userId) {
        return formResultsService.getUserFormResults(userId);
    }

    @GetMapping("/user/{userId}/tests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public TestResultByUser getUserTestResults(@PathVariable int userId) {
        return testResultsService.getUserTestResults(userId);
    }

    @GetMapping("/user/{userId}/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResultsByUser getUserAllResults(@PathVariable int userId) {
        return resultsService.getUserAllResults(userId);
    }

    @GetMapping("/group/{groupId}/forms")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getGroupFormResultsZip(@PathVariable int groupId) {
        try {
            final FormAnswersByGroup groupResults = formResultsService.getGroupFormResults(groupId);
            final byte[] jsonBytes = objectMapper.writeValueAsBytes(groupResults);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            final byte[] gzipBytes = baos.toByteArray();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"group-form-results-" + groupId + ".json.gz\""
            );
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/group/{groupId}/tests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getGroupTestResultsZip(@PathVariable int groupId) {
        try {
            final TestResultsByGroup groupResults = testResultsService.getGroupTestResults(groupId);
            final byte[] jsonBytes = objectMapper.writeValueAsBytes(groupResults);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            final byte[] gzipBytes = baos.toByteArray();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group-test-results-" + groupId +
                                                 ".json.gz\""
            );
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/group/{groupId}/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getGroupFullResultsZip(@PathVariable int groupId) {
        try {
            final ResultsByGroup groupResults = resultsService.getGroupFullResults(groupId);
            final byte[] jsonBytes = objectMapper.writeValueAsBytes(groupResults);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            final byte[] gzipBytes = baos.toByteArray();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group-full-results-" + groupId +
                                                 ".json.gz\""
            );
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/all/latest/zip")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllUsersLatestConfigResultsZip() {
        try {
            // ConfigModel lastConfig = configRepository.findLastConfig();
            // TODO changed from findAllByGroup_Config_Version(lastConfig.getVersion());
            final List<UserModel> users = userRepository.getAllUsers();

            final List<ResultsByUser> userResults = users.stream()
                .map(user -> resultsService.getUserAllResults(user.getId()))
                .toList();

            final byte[] jsonBytes = objectMapper.writeValueAsBytes(userResults);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            final byte[] gzipBytes = baos.toByteArray();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"all-users-latest-config-results.json" +
                                                 ".gz\""
            );
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/all/latest/forms/zip")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllUsersLatestConfigFormsZip() {
        try {
            // ConfigModel lastConfig = configRepository.findLastConfig();
            // TODO changed from findAllByGroup_Config_Version(lastConfig.getVersion());
            final List<UserModel> users = userRepository.getAllUsers();

            final List<FormAnswersByUser> userFormResults = users.stream()
                .map(user -> formResultsService.getUserFormResults(user.getId()))
                .toList();

            final byte[] jsonBytes = objectMapper.writeValueAsBytes(userFormResults);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            final byte[] gzipBytes = baos.toByteArray();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"all-users-latest-config-forms.json" +
                                                 ".gz\""
            );
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/all/latest/tests/zip")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllUsersLatestConfigTestsZip() {
        try {
            // ConfigModel lastConfig = configRepository.findLastConfig();
            // TODO changed from findAllByGroup_Config_Version(lastConfig.getVersion());
            final List<UserModel> users = userRepository.getAllUsers();

            final List<TestResultByUser> userTestResults = users.stream()
                .map(user -> testResultsService.getUserTestResults(user.getId()))
                .toList();

            final byte[] jsonBytes = objectMapper.writeValueAsBytes(userTestResults);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(jsonBytes);
            }
            final byte[] gzipBytes = baos.toByteArray();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/gzip"));
            headers.set(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"all-users-latest-config-tests.json" +
                                                 ".gz\""
            );
            headers.setContentLength(gzipBytes.length);

            return ResponseEntity.ok().headers(headers).body(gzipBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
