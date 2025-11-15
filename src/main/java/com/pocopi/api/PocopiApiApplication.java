package com.pocopi.api;

import com.pocopi.api.migration.OldConfigMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PocopiApiApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(PocopiApiApplication.class);
    private static final String MIGRATE_OLD_CONFIG_ARG = "migrate-old-config";
    private static final String MIGRATE_OLD_CONFIG_PATH = ".old-config";

    public static void main(final String[] args) {
        SpringApplication.run(PocopiApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner runMigration(OldConfigMigrator migrator, ApplicationArguments args) {
        return cmdArgs -> {
            if (!args.containsOption(MIGRATE_OLD_CONFIG_ARG)) {
                return;
            }

            try {
                LOGGER.info("Migrating old configuration...");
                migrator.migrate(MIGRATE_OLD_CONFIG_PATH);
                LOGGER.info("Successfully migrated old configuration");
                System.exit(0);
            } catch (Exception e) {
                LOGGER.error("Failed to migrate old configuration from path: {}", MIGRATE_OLD_CONFIG_PATH, e);
                System.exit(1);
            }
        };
    }
}
