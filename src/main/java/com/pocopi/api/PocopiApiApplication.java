package com.pocopi.api;

import com.pocopi.api.dto.user.NewAdmin;
import com.pocopi.api.exception.MultiFieldException;
import com.pocopi.api.migration.OldConfigMigrator;
import com.pocopi.api.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootApplication
public class PocopiApiApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(PocopiApiApplication.class);
    private static final String CREATE_ADMIN_ARG = "create-admin";
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

    @Bean
    public CommandLineRunner createAdmin(UserService userService, ApplicationArguments args) {
        return cmdArgs -> {
            if (!args.containsOption(CREATE_ADMIN_ARG)) {
                return;
            }

            try {
                while (true) {
                    try {
                        final String username;
                        final String password;

                        final Console console = System.console();

                        if (console != null) {
                            username = console.readLine("Enter admin username: ");

                            final char[] passwordChars = console.readPassword("Enter admin password: ");
                            password = new String(passwordChars);
                            Arrays.fill(passwordChars, ' ');
                        } else {
                            final Scanner scanner = new Scanner(System.in);
                            System.out.print("Enter admin username: ");
                            username = scanner.nextLine();

                            System.out.print("Enter admin password: ");
                            password = scanner.nextLine();
                        }

                        userService.createAdmin(new NewAdmin(username, password));

                        LOGGER.info("Successfully created admin: {}", username);
                        System.exit(0);
                    } catch (MultiFieldException e) {
                        final String errors = e.getErrors().stream()
                            .map(error -> "    " + error.field() + ": " + error.message())
                            .collect(Collectors.joining("\n"));

                        LOGGER.error("{}:\n{}", e.getMessage(), errors);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create admin", e);
                System.exit(1);
            }
        };
    }
}
