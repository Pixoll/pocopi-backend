package com.pocopi.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.results.FormAnswersByUser;
import com.pocopi.api.dto.results.ResultsByUser;
import com.pocopi.api.dto.results.TestResultsByUser;
import com.pocopi.api.services.ResultsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

@RestController
@RequestMapping("/api/results")
@Tag(name = "Results")
public class ResultsController {
    private static final String GZIP_MIME_TYPE = "application/gzip";
    private static final MediaType GZIP_MEDIA_TYPE = MediaType.valueOf(GZIP_MIME_TYPE);

    private final ResultsService resultsService;
    private final ObjectMapper objectMapper;

    public ResultsController(ResultsService resultsService, ObjectMapper objectMapper) {
        this.resultsService = resultsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = {GZIP_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllResults() {
        final List<ResultsByUser> userResults = resultsService.getAllResults();
        return compressResults(userResults, (r) -> r.user().username(), "results");
    }

    @GetMapping(path = "/forms", produces = {GZIP_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllFormResults() {
        final List<FormAnswersByUser> userFormResults = resultsService.getAllFormResults();
        return compressResults(userFormResults, (r) -> r.user().username(), "form-results");
    }

    @GetMapping(path = "/tests", produces = {GZIP_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllTestResults() {
        final List<TestResultsByUser> userTestResults = resultsService.getAllTestResults();
        return compressResults(userTestResults, (r) -> r.user().username(), "test-results");
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

    private <T> ResponseEntity<byte[]> compressResults(
        List<T> results,
        Function<T, String> resultToFileName,
        String compressedFileName
    ) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (
            final GZIPOutputStream gzipOutput = new GZIPOutputStream(baos);
            final TarArchiveOutputStream tarOutput = new TarArchiveOutputStream(gzipOutput)
        ) {
            tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

            for (final T result : results) {
                final String filename = resultToFileName.apply(result);
                final String content = convertToJson(result);
                final byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

                final TarArchiveEntry tarEntry = new TarArchiveEntry(filename + ".json");
                tarEntry.setSize(contentBytes.length);
                tarEntry.setModTime(System.currentTimeMillis());

                tarOutput.putArchiveEntry(tarEntry);
                tarOutput.write(contentBytes);
                tarOutput.closeArchiveEntry();
            }

            tarOutput.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final byte[] tarballBytes = baos.toByteArray();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(GZIP_MEDIA_TYPE);
        headers.setContentDisposition(ContentDisposition.attachment().filename(compressedFileName + ".tar.gz").build());
        headers.setContentLength(tarballBytes.length);

        return ResponseEntity.ok().headers(headers).body(tarballBytes);
    }

    private <T> String convertToJson(T result) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting result to JSON", e);
        }
    }
}
