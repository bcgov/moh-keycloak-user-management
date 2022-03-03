package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import io.netty.util.internal.SocketUtils;
import net.minidev.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.*;



@RestController
public class OrganizationsController {

    //todo: write the url, user and password somewhere else
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.user}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.database}")
    private String databaseName;

    public OrganizationsController(WebClientService webClientService, @Value("${config.vanity-hostname}") String vanityHostname) {
    }

    public PreparedStatement connectAndPrepareStatement(String sql) throws SQLException{
        return DriverManager.getConnection(url, user, password).prepareStatement(sql);
    }

    @GetMapping("/organizations")
    public Collection<JSONObject> getOrganizations() {
        Collection<JSONObject> result = new LinkedList<JSONObject>();
        String SQL = "SELECT * FROM " + databaseName;

        try {
            PreparedStatement pstmt = this.connectAndPrepareStatement(SQL);
            ResultSet rs = pstmt.executeQuery();
        
            while (rs.next()) {
                String id = rs.getString("organization_id");
                String name = rs.getString("organization_name");
                result.add(new JSONObject(Map.of("id", id, "name", name)));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    @GetMapping("/organizations/{organizationId}")
    public JSONObject getOrganization(@PathVariable String organizationId) throws SQLException {
        String SQL = "SELECT * FROM " + databaseName;

            PreparedStatement pstmt = this.connectAndPrepareStatement(SQL);
            ResultSet rs = pstmt.executeQuery();
            rs.next();    

            String id = rs.getString("organization_id");
            String name = rs.getString("organization_name");
            return new JSONObject(Map.of("id", id, "name", name));
    }


    @PostMapping("/organizations")
    public void createOrganization(@RequestBody JSONObject body) throws SQLException {
        String SQL = "INSERT INTO organizations(organization_id,organization_name) "
                + "VALUES(?,?)";

            PreparedStatement pstmt = this.connectAndPrepareStatement(SQL);

            pstmt.setString(1, (String) body.get("id"));
            pstmt.setString(2, (String) body.get("name"));

            pstmt.executeUpdate();
    }

}
