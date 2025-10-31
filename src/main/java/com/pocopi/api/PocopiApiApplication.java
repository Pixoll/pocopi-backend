package com.pocopi.api;

import com.pocopi.api.migration.OldConfigMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class PocopiApiApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(PocopiApiApplication.class);
    private static final String MIGRATE_OLD_CONFIG_ARG = "migrate-old-config";

    public static void main(final String[] args) {
        SpringApplication.run(PocopiApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner runMigration(OldConfigMigrator migrator, ApplicationArguments args) {
        return cmdArgs -> {
            if (!args.containsOption(MIGRATE_OLD_CONFIG_ARG)) {
                return;
            }

            final String oldConfigPath = args.getOptionValues(MIGRATE_OLD_CONFIG_ARG) != null
                                         && !args.getOptionValues(MIGRATE_OLD_CONFIG_ARG).isEmpty()
                ? args.getOptionValues(MIGRATE_OLD_CONFIG_ARG).getFirst()
                : null;

            if (oldConfigPath == null || oldConfigPath.trim().isEmpty()) {
                LOGGER.error(
                    "You must specify the path of the old configuration in the " + MIGRATE_OLD_CONFIG_ARG
                    + " option like so: --" + MIGRATE_OLD_CONFIG_ARG + "=\"/path/to/old-config\""
                );
                return;
            }

            try {
                migrator.migrate(oldConfigPath);
            } catch (IOException e) {
                LOGGER.error("Failed to migrate old configuration from path: {}", oldConfigPath, e);
            }
        };
    }
}
