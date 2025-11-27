package com.pocopi.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integration")
class DatabaseConnectionIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Test
    void testDatabaseConnectionIsValid() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(2), "La conexión a la base de datos debe ser válida");
        }
    }
}
