package com.pocopi.api.integration;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integration")
class DatabaseConnectionIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionIntegrationTest.class);

    @Autowired
    DataSource dataSource;

    @Test
    void testDatabaseConnectionIsValid() throws Exception {
        log.info(">> [DB-CONNECTION] Iniciando prueba de conexión a la base de datos (integration profile)");

        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            log.info(">> [DB-CONNECTION] Resultado de conn.isValid(2): {}", valid);

            assertTrue(valid, "La conexión a la base de datos debe ser válida");
            log.info(">> [DB-CONNECTION] Conexión a la base de datos verificada correctamente");
        }
    }
}
