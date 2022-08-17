package ca.bc.gov.hlth.mohums.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
public class RealmController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/realms")
    public List<Map<String, Object>> getActiveUserCount() throws SQLException {
        String sql = "SELECT DISTINCT realm_id AS REALM, value AS DESCRIPTION FROM keycloak.realm_attribute WHERE name = 'displayName'";
        return jdbcTemplate.queryForList(sql);
    }
}
