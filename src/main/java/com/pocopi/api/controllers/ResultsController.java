package com.pocopi.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.csv.ResultCsv;
import com.pocopi.api.dto.results.FormAnswersByUser;
import com.pocopi.api.dto.results.ResultsByUser;
import com.pocopi.api.dto.results.TestResultsByUser;
import com.pocopi.api.mappers.UserResultsMapper;
import com.pocopi.api.services.ResultsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    private static final String CSV_MIME_TYPE = "text/csv";
    private static final String GZIP_MIME_TYPE = "application/gzip";
    private static final MediaType GZIP_MEDIA_TYPE = MediaType.valueOf(GZIP_MIME_TYPE);

    private final ResultsService resultsService;
    private final ObjectMapper objectMapper;
    private final UserResultsMapper userResultsMapper;

    public ResultsController(
        ResultsService resultsService,
        ObjectMapper objectMapper,
        UserResultsMapper userResultsMapper
    ) {
        this.resultsService = resultsService;
        this.objectMapper = objectMapper;
        this.userResultsMapper = userResultsMapper;
    }

    @GetMapping(produces = {GZIP_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllResults(@RequestParam(defaultValue = "false") boolean csv) {
        final List<ResultsByUser> userResults = resultsService.getAllResults();

        if (!csv) {
            return compressResults(
                userResults,
                (r) -> r.user().username() + ".json",
                this::convertToJson,
                "results"
            );
        }

        final List<ResultCsv> resultsCsv = userResults.stream()
            .flatMap(r -> userResultsMapper.userResultsToCsv(r).stream())
            .toList();

        return compressResults(
            resultsCsv,
            (r) -> r.username() + "-" + r.type() + ".csv",
            ResultCsv::csv,
            "results"
        );
    }

    @GetMapping(path = "/forms", produces = {GZIP_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllFormResults(@RequestParam(defaultValue = "false") boolean csv) {
        final List<FormAnswersByUser> userFormResults = resultsService.getAllFormResults();
        final String extension = csv ? ".csv" : ".json";

        return compressResults(
            userFormResults,
            (r) -> r.user().username() + extension,
            !csv ? this::convertToJson : userResultsMapper::userFormResultsToCsv,
            "form-results"
        );
    }

    @GetMapping(path = "/tests", produces = {GZIP_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getAllTestResults(@RequestParam(defaultValue = "false") boolean csv) {
        final List<TestResultsByUser> userTestResults = resultsService.getAllTestResults();
        final String extension = csv ? ".csv" : ".json";

        return compressResults(
            userTestResults,
            (r) -> r.user().username() + extension,
            !csv ? this::convertToJson : userResultsMapper::userTestResultsToCsv,
            "test-results"
        );
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResultsByUser getUserResults(@PathVariable int userId) {
        return resultsService.getUserResults(userId);
    }

    @GetMapping(path = "/users/{userId}/csv", produces = {GZIP_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> getUserResultsCsv(@PathVariable int userId) {
        final ResultsByUser userResults = resultsService.getUserResults(userId);
        final List<ResultCsv> resultsCsv = userResultsMapper.userResultsToCsv(userResults);

        return compressResults(
            resultsCsv,
            (r) -> r.type() + ".csv",
            ResultCsv::csv,
            "results"
        );
    }

    @GetMapping("/users/{userId}/forms")
    @PreAuthorize("hasAuthority('ADMIN')")
    public FormAnswersByUser getUserFormResults(@PathVariable int userId) {
        return resultsService.getUserFormResults(userId);
    }

    @GetMapping(path = "/users/{userId}/forms/csv", produces = {CSV_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getUserFormResultsCsv(@PathVariable int userId) {
        return userResultsMapper.userFormResultsToCsv(resultsService.getUserFormResults(userId));
    }

    @GetMapping("/users/{userId}/tests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public TestResultsByUser getUserTestResults(@PathVariable int userId) {
        return resultsService.getUserTestResults(userId);
    }

    @GetMapping(path = "/users/{userId}/tests/csv", produces = {CSV_MIME_TYPE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getUserTestResultsCsv(@PathVariable int userId) {
        return userResultsMapper.userTestResultsToCsv(resultsService.getUserTestResults(userId));
    }

    private <T> ResponseEntity<byte[]> compressResults(
        List<T> results,
        Function<T, String> resultToFileName,
        Function<T, String> resultToFileContentsString,
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
                final String content = resultToFileContentsString.apply(result);
                final byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

                final TarArchiveEntry tarEntry = new TarArchiveEntry(filename);
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
