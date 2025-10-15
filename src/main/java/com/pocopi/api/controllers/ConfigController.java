package com.pocopi.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.Config.PatchLastConfig;
import com.pocopi.api.dto.Config.PatchRequest;
import com.pocopi.api.dto.Config.PatchResponse;
import com.pocopi.api.dto.Config.SingleConfigResponse;
import com.pocopi.api.services.interfaces.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/latest")
    public ResponseEntity<SingleConfigResponse> getLastestConfig() {
        SingleConfigResponse response = configService.getLastConfig();
        return ResponseEntity.ok(response); 
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatchResponse> updateConfig(
        @RequestPart(value = "appIcon", required = false) MultipartFile appIcon,
        @RequestParam("preTestFormQuestionOptionsFiles") MultipartFile[] preTestFiles,
        @RequestParam("preTestFormQuestionOptionsIndexes") String preTestIndexesJson,
        @RequestParam("postTestFormQuestionOptionsFiles") MultipartFile[] postTestFiles,
        @RequestParam("postTestFormQuestionOptionsIndexes") String postTestIndexesJson,
        @RequestParam("groupQuestionOptionsFiles") MultipartFile[] groupFiles,
        @RequestParam("groupQuestionOptionsIndexes") String groupIndexesJson,
        @RequestParam("informationCardFiles") MultipartFile[] cardFiles,
        @RequestParam("informationCardIndexes") String cardIndexesJson,
        @RequestPart("updateLastConfig") String updateLastConfigJson
    ) throws JsonProcessingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        Integer[] preTestIndexes = mapper.readValue(preTestIndexesJson, Integer[].class);
        Integer[] postTestIndexes = mapper.readValue(postTestIndexesJson, Integer[].class);
        Integer[] groupIndexes = mapper.readValue(groupIndexesJson, Integer[].class);
        Integer[] cardIndexes = mapper.readValue(cardIndexesJson, Integer[].class);

        Map<Integer, MultipartFile> preTestMap = buildFileMap(preTestFiles, preTestIndexes);
        Map<Integer, MultipartFile> postTestMap = buildFileMap(postTestFiles, postTestIndexes);
        Map<Integer, MultipartFile> groupMap = buildFileMap(groupFiles, groupIndexes);
        Map<Integer, MultipartFile> cardMap = buildFileMap(cardFiles, cardIndexes);

        PatchLastConfig config = mapper.readValue(updateLastConfigJson, PatchLastConfig.class);

        PatchRequest request = new PatchRequest(
            Optional.ofNullable(appIcon),
            preTestMap,
            postTestMap,
            groupMap,
            cardMap,
            config
        );

        PatchResponse response = configService.processUpdatedConfig(request);
        return ResponseEntity.ok(response);
    }

    private Map<Integer, MultipartFile> buildFileMap(MultipartFile[] files, Integer[] indexes) {
        Map<Integer, MultipartFile> map = new HashMap<>();
        for (int i = 0; i < files.length; i++) {
            map.put(indexes[i], files[i]);
        }
        return map;
    }
}
