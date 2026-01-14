package com.techcorp.employee.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigurationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldUseTestDatabaseProfile() throws SQLException {
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        String url = metaData.getURL();
        String username = metaData.getUserName();

        System.out.println("=== DATABASE CONFIGURATION ===");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("Database: " + metaData.getDatabaseProductName());
        System.out.println("Version: " + metaData.getDatabaseProductVersion());
        System.out.println("==============================");

        // Verify test database
        assertThat(url).contains("employeedbtest");
        assertThat(username.toLowerCase()).isEqualTo("sa");
    }

    @Test
    void shouldBeAbleToCreateTablesInTestDatabase() {
        // Test that we can actually use the database
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(255))");
        jdbcTemplate.execute("INSERT INTO test_table (id, name) VALUES (1, 'test')");

        String result = jdbcTemplate.queryForObject(
                "SELECT name FROM test_table WHERE id = 1", String.class);

        assertThat(result).isEqualTo("test");

        // Cleanup
        jdbcTemplate.execute("DROP TABLE test_table");
    }
}