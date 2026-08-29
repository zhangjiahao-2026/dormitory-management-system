package com.example.springboot.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class LocalAiSchemaConfigTest {

    @Test
    void initializesAiRepairSchemaAfterBaseRepairTable() throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:ai-schema;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                "");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE repair (id INT PRIMARY KEY)");
        }

        new LocalAiSchemaConfig().aiRepairSchemaInitializer(dataSource).afterPropertiesSet();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet table = statement.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.tables "
                             + "WHERE table_name = 'ai_repair_request'")) {
            table.next();
            assertEquals(1, table.getInt(1));
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet column = statement.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.columns "
                             + "WHERE table_name = 'repair' AND column_name = 'ai_request_id'")) {
            column.next();
            assertEquals(1, column.getInt(1));
        }
    }
}
