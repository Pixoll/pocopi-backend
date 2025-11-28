package com.pocopi.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.results.FormAnswersByUser;
import com.pocopi.api.dto.results.ResultsByUser;
import com.pocopi.api.dto.results.TestResultsByUser;
import com.pocopi.api.models.user.UserModel;
import com.pocopi.api.repositories.UserRepository;
import com.pocopi.api.services.ResultsService;
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
    private final ResultsService resultsService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public ResultsController(
        ResultsService resultsService,
        ObjectMapper objectMapper,
        UserRepository userRepository
    ) {
        this.resultsService = resultsService;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @GetMapping("/zip")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllUsersLatestConfigResultsZip() {
        try {
            // ConfigModel lastConfig = configRepository.findLastConfig();
            // TODO changed from findAllByGroup_Config_Version(lastConfig.getVersion());
            final List<UserModel> users = userRepository.findAll();

            final List<ResultsByUser> userResults = users.stream()
                .map(user -> resultsService.getUserResults(user.getId()))
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

    @GetMapping("/forms/zip")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllUsersLatestConfigFormsZip() {
        try {
            // ConfigModel lastConfig = configRepository.findLastConfig();
            // TODO changed from findAllByGroup_Config_Version(lastConfig.getVersion());
            final List<UserModel> users = userRepository.findAll();

            final List<FormAnswersByUser> userFormResults = users.stream()
                .map(user -> resultsService.getUserFormResults(user.getId()))
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

    @GetMapping("/tests/zip")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllUsersLatestConfigTestsZip() {
        try {
            // ConfigModel lastConfig = configRepository.findLastConfig();
            // TODO changed from findAllByGroup_Config_Version(lastConfig.getVersion());
            final List<UserModel> users = userRepository.findAll();

            final List<TestResultsByUser> userTestResults = users.stream()
                .map(user -> resultsService.getUserTestResults(user.getId()))
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

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResultsByUser getUserResults(@PathVariable int userId) {
        return resultsService.getUserResults(userId);
    }

    @GetMapping("/users/{userId}/forms")
    @PreAuthorize("hasAuthority('ADMIN')")
    public FormAnswersByUser getUserFormResults(@PathVariable int userId) {
        return resultsService.getUserFormResults(userId);
    }

    @GetMapping("/users/{userId}/tests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public TestResultsByUser getUserTestResults(@PathVariable int userId) {
        return resultsService.getUserTestResults(userId);
    }
}
