package com.pocopi.api.migration;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

public final class OldConfigReader {
    private static final Yaml YAML_PARSER = new Yaml();
    private static final SchemaRegistry SCHEMA_REGISTRY = SchemaRegistry.withDialect(Dialects.getDraft202012());

    public static LinkedHashMap<String, Object> read(String oldConfigPathString) throws IOException {
        final Path oldConfigPath = Paths.get(oldConfigPathString);
        final Path jsonSchemasPath = oldConfigPath.resolve("schemas");

        final Path formsConfigPath = oldConfigPath.resolve("forms.yaml");
        final Path homeConfigPath = oldConfigPath.resolve("home.yaml");
        final Path testConfigPath = oldConfigPath.resolve("test.yaml");
        final Path translationsConfigPath = oldConfigPath.resolve("translations.yaml");

        final Path formsJsonSchemaPath = jsonSchemasPath.resolve("forms.json");
        final Path homeJsonSchemaPath = jsonSchemasPath.resolve("home.json");
        final Path testJsonSchemaPath = jsonSchemasPath.resolve("test.json");
        final Path translationsJsonSchemaPath = jsonSchemasPath.resolve("translations.json");

        final LinkedHashMap<String, Object> formsConfig = getConfig(formsConfigPath, formsJsonSchemaPath);
        final LinkedHashMap<String, Object> homeConfig = getConfig(homeConfigPath, homeJsonSchemaPath);
        final LinkedHashMap<String, Object> testConfig = getConfig(testConfigPath, testJsonSchemaPath);
        final LinkedHashMap<String, Object> translationsConfig = getConfig(
            translationsConfigPath,
            translationsJsonSchemaPath
        );

        final HashMap<String, Path> imagePaths = getImagePaths(oldConfigPathString);

        final LinkedHashMap<String, Object> config = new LinkedHashMap<>();

        config.putAll(formsConfig);
        config.putAll(homeConfig);
        config.putAll(testConfig);
        config.put("translations", translationsConfig);
        config.put("images", imagePaths);

        return config;
    }

    private static LinkedHashMap<String, Object> getConfig(Path configPath, Path jsonSchemaPath)
        throws IOException {
        final Schema schema;
        final String yamlString;

        try (final FileInputStream inputStream = new FileInputStream(jsonSchemaPath.toFile())) {
            schema = SCHEMA_REGISTRY.getSchema(inputStream);
        }

        try (final FileInputStream inputStream = new FileInputStream(configPath.toFile())) {
            yamlString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        final List<com.networknt.schema.Error> errors = schema.validate(
            yamlString,
            InputFormat.YAML,
            executionContext -> executionContext.executionConfig(executionConfig ->
                executionConfig.formatAssertionsEnabled(true)
            )
        );

        if (errors.isEmpty()) {
            return YAML_PARSER.load(yamlString);
        }

        final HashMap<String, String> errorsMap = new HashMap<>();
        for (final Error error : errors) {
            final String key = error.getInstanceLocation().toString();

            if (!errorsMap.containsKey(key)) {
                errorsMap.put(key, error.getMessage());
                continue;
            }

            if (error.getMessageKey().equals("enum")
                && error.getArguments() != null
                && error.getArguments().length > 0
            ) {
                errorsMap.compute(key, (k, message) -> message + " " + error.getArguments()[0]);
            }
        }

        final StringBuilder errorMessage = new StringBuilder()
            .append("\nThe config ")
            .append(configPath.getFileName())
            .append(" contains the following errors:\n");

        errorsMap.forEach((key, message) -> {
            final String location = key.startsWith("/")
                ? key.substring(1).replaceAll("/", ".")
                : key.replaceAll("/", ".");

            errorMessage
                .append("\n    Location:      ")
                .append(location)
                .append("\n    Error message: ")
                .append(message)
                .append("\n");
        });

        throw new IllegalArgumentException(errorMessage.toString());
    }

    private static HashMap<String, Path> getImagePaths(String yamlConfigDirPath) throws IOException {
        final Path rootPath = Paths.get(yamlConfigDirPath, "images").toAbsolutePath().normalize();
        final HashMap<String, Path> imagesMap = new HashMap<>();

        try (final Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                .forEach(filePath -> {
                    final Path absolutePath = filePath.toAbsolutePath();
                    final Path relativePath = rootPath.relativize(absolutePath);
                    final String key = relativePath.toString().replace("\\", "/");
                    imagesMap.put(key, absolutePath);
                });
        }

        return imagesMap;
    }
}
