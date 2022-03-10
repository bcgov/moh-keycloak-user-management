package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;


@RestController
public class OrganizationsController {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.database}")
    private String database;
    @Value("${spring.datasource.organization_id}")
    private String organization_id;
    @Value("${spring.datasource.organization_name}")
    private String organization_name;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public PreparedStatement connectAndPrepareStatement(String sql) throws SQLException {
        return DriverManager.getConnection(url, user, password).prepareStatement(sql);
    }

    @GetMapping("/organizations")
    public Collection<Object> getOrganizations() throws SQLException {
        return jdbcTemplate.query("SELECT * FROM " + database + " ORDER BY " + organization_id,
                (rs, rowNum) -> Map.of("id", rs.getString(organization_id),
                        "name", rs.getString(organization_name)));
    }

    @PostMapping("/organizations")
    public void createOrganization(@RequestBody JSONObject body) throws SQLException {
        jdbcTemplate.update("insert into " + database + " ("+organization_id + ", " + organization_name + ") values (?,?)",
                body.get("id"), body.get("name"));
    }

}
