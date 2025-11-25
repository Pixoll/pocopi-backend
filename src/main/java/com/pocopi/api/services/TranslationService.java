package com.pocopi.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocopi.api.dto.config.Translation;
import com.pocopi.api.dto.config.TranslationUpdate;
import com.pocopi.api.exception.HttpException;
import com.pocopi.api.models.config.ConfigModel;
import com.pocopi.api.models.config.TranslationKeyModel;
import com.pocopi.api.models.config.TranslationValueModel;
import com.pocopi.api.repositories.TranslationKeyRepository;
import com.pocopi.api.repositories.TranslationValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TranslationService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TranslationValueRepository translationValueRepository;
    private final TranslationKeyRepository translationKeyRepository;

    public TranslationService(
        TranslationValueRepository translationValueRepository,
        TranslationKeyRepository translationKeyRepository
    ) {
        this.translationValueRepository = translationValueRepository;
        this.translationKeyRepository = translationKeyRepository;
    }

    public Map<String, String> getAllTranslationKeyValues(int configVersion) {
        return translationValueRepository
            .findAllKeyValuePairsByConfigVersion(configVersion)
            .stream()
            .collect(
                HashMap::new,
                (map, translation) -> map.put(translation.getKey(), translation.getValue()),
                HashMap::putAll
            );
    }

    public List<Translation> getAllTranslations(int configVersion) {
        return translationValueRepository
            .findAllByConfigVersionWithDetails(configVersion)
            .stream()
            .map((translation) -> new Translation(
                translation.getKey(),
                translation.getValue(),
                translation.getDescription(),
                parseJsonStringArray(translation.getArgumentsJson())
            ))
            .toList();
    }

    @Transactional
    public boolean updateTranslations(ConfigModel config, List<TranslationUpdate> translationUpdates) {
        final Map<String, TranslationKeyModel> translationKeys = translationKeyRepository.findAll().stream()
            .collect(Collectors.toMap(TranslationKeyModel::getKey, key -> key, (a, b) -> b));

        final Map<String, TranslationValueModel> storedTranslationValues = translationValueRepository
            .findAllByConfigVersion(config.getVersion())
            .stream()
            .collect(Collectors.toMap((t) -> t.getKey().getKey(), (t) -> t, (a, b) -> b));

        boolean modified = false;

        for (final TranslationUpdate translationUpdate : translationUpdates) {
            final TranslationKeyModel translationKey = translationKeys.get(translationUpdate.key());

            if (translationKey == null) {
                throw HttpException.notFound("Translation key " + translationUpdate.key() + " not found");
            }

            final TranslationValueModel storedValue = storedTranslationValues.get(translationUpdate.key());

            if (storedValue == null) {
                final TranslationValueModel newTranslationValue = TranslationValueModel.builder()
                    .config(config)
                    .key(translationKey)
                    .value(translationUpdate.value())
                    .build();

                translationValueRepository.save(newTranslationValue);
                modified = true;

                continue;
            }

            if (Objects.equals(storedValue.getValue(), translationUpdate.value())) {
                continue;
            }

            storedValue.setValue(translationUpdate.value());

            translationValueRepository.save(storedValue);
            modified = true;
        }

        return modified;
    }

    @Transactional
    public void cloneTranslations(int originalConfigVersion, ConfigModel config) {
        final List<TranslationValueModel> translationValues = translationValueRepository
            .findAllByConfigVersion(originalConfigVersion);

        for (final TranslationValueModel translationValue : translationValues) {
            final TranslationValueModel newTranslationValue = TranslationValueModel.builder()
                .config(config)
                .key(translationValue.getKey())
                .value(translationValue.getValue())
                .build();

            translationValueRepository.save(newTranslationValue);
        }
    }

    private static List<String> parseJsonStringArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return OBJECT_MAPPER.readValue(
                json, new TypeReference<>() {
                }
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse arguments JSON", e);
        }
    }
}
