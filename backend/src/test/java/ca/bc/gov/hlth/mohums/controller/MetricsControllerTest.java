package ca.bc.gov.hlth.mohums.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.DriverManager;
import java.sql.SQLException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(Lifecycle.PER_CLASS)
class MetricsControllerTest {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testConnection() {
        try {
            DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            Assertions.fail("failed to connect with database");
        }
    }

    @Test
    void testJdbcCanQuery() {
        try {
            String sql = "SELECT DISTINCT id, realm_id FROM user_entity";
            jdbcTemplate.queryForList(sql);
        } catch (DataAccessException e) {
            Assertions.fail("failed to query");
        }
    }
}