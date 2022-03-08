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

    //todo: write the url, user and password somewhere else
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.database}")
    private String database;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public OrganizationsController(WebClientService webClientService, @Value("${config.vanity-hostname}") String vanityHostname) {
    }

    public PreparedStatement connectAndPrepareStatement(String sql) throws SQLException {
        return DriverManager.getConnection(url, user, password).prepareStatement(sql);
    }

    @GetMapping("/organizations")
    public Collection<Object> getOrganizations() throws SQLException {
        return jdbcTemplate.query("SELECT * FROM " + database + " ORDER BY organization_id",
                (rs, rowNum) -> Map.of("id", rs.getString("organization_id"),
                        "name", rs.getString("organization_name")));
    }

    @GetMapping("/organizations/{organizationId}")
    public Object getOrganization(@PathVariable String organizationId) throws SQLException {
        return jdbcTemplate.queryForObject("SELECT * FROM " + database + " WHERE organization_id = ?", new Object[]{organizationId},
                (rs, rowNum) -> Map.of("id", rs.getString("organization_id"), "name", rs.getString("organization_name"))
        );
    }


    @PostMapping("/organizations")
    public void createOrganization(@RequestBody JSONObject body) throws SQLException {
        jdbcTemplate.update("insert into " + database + " (organization_id, organization_name) values (?,?)",
                body.get("id"), body.get("name"));
    }

}
