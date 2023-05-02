package ca.bc.gov.hlth.mohums.controller;

import java.util.List;
import java.util.Map;
import java.sql.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/metrics/active-user-count")
    public List<Map<String, Object>> getActiveUserCount() throws SQLException {
        String sql
                = "SELECT realm_id AS REALM, client_id AS CLIENT, COUNT(1) AS ACTIVE_USER_COUNT, description AS DESCRIPTION"
                + "  FROM ("
                + "    SELECT DISTINCT ue.realm_id, c.client_id, ue.username, c.description"
                + "      FROM keycloak.event_entity ee"
                + "     INNER JOIN keycloak.user_entity ue ON ue.id = ee.user_id"
                + "     INNER JOIN keycloak.client c ON c.client_id = ee.client_id AND c.realm_id = ee.realm_id"
                + "     INNER JOIN keycloak.keycloak_role kcr ON kcr.client = c.id"
                + "     INNER JOIN keycloak.user_role_mapping urm ON urm.user_id = ee.user_id AND urm.role_id = kcr.id"
                + "     WHERE ee.type = 'LOGIN'"
                + "       AND ee.event_time > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000"
                + "       AND ue.enabled = 1"
                + "     UNION"
                + "    SELECT DISTINCT ue.realm_id, c.client_id, ue.username, c.description"
                + "      FROM keycloak.event_entity ee"
                + "     INNER JOIN keycloak.user_entity ue ON ue.id = ee.user_id"
                + "     INNER JOIN keycloak.client c ON c.client_id = ee.client_id AND c.realm_id = ee.realm_id"
                + "      LEFT OUTER JOIN keycloak.keycloak_role kcr ON kcr.client = c.id"
                + "     WHERE ee.type = 'LOGIN'"
                + "       AND ee.event_time > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000"
                + "       AND ue.enabled = 1"
                + "       AND kcr.id IS NULL"
                + " )"
                + " WHERE NOT (client_id = 'account' AND (LOWER(realm_id) IN ('bceid_basic', 'bceid_business', 'bcprovider_aad', 'bcsc', 'idir', 'idir_aad', 'master', 'moh_applications', 'moh_citizen', 'moh_idp', 'phsa', 'phsa_aad'))"
                + "          OR (client_id IN ('realm-management', 'USER-MANAGEMENT-SERVICE', 'PRIME-WEBAPP-ENROLLMENT')))"
                + " GROUP BY realm_id, client_id, description"
                + " ORDER BY realm_id ASC, client_id ASC";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/metrics/total-active-user-count")
    public List<Map<String, Object>> getTotalActiveUserCountYear() throws SQLException {
        String sql
                = "SELECT EVENT_DATE, COUNT(1) AS ACTIVE_USER_COUNT"
                + "  FROM ("
                + "    SELECT TO_CHAR(FROM_TZ(CAST(to_date('19700101', 'YYYYMMDD') + NUMTODSINTERVAL(event_time/1000, 'SECOND') AS timestamp), 'UTC') AT TIME ZONE 'America/Vancouver', 'YYYY-MM-DD') AS EVENT_DATE"
                + "    FROM keycloak.event_entity"
                + "    WHERE type = 'LOGIN'"
                + "  )"
                + " WHERE EVENT_DATE > ADD_MONTHS(CURRENT_DATE, -12)"
                + " GROUP BY EVENT_DATE"
                + " ORDER BY EVENT_DATE DESC";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/metrics/total-number-of-users")
    public Object getTotalNumberOfUsers() throws SQLException {
        String sql
                = "SELECT COUNT(1) AS TOTAL_USER_COUNT"
                + "  FROM ("
                + "    SELECT DISTINCT id, realm_id"
                + "      FROM keycloak.user_entity"
                + "     WHERE (LOWER(realm_id) IN ('bceid_basic', 'bceid_business', 'bcprovider_aad', 'bcsc', 'idir', 'mhsu_ehs', 'moh_idp', 'phsa'))"
                + "        OR (realm_id = 'idir_aad' AND username NOT IN (SELECT username FROM keycloak.user_entity WHERE realm_id = 'idir'))"
                + "        OR (realm_id = 'mhsu_foundry' AND username NOT IN (SELECT username FROM keycloak.user_entity WHERE realm_id = 'mhsu_ehs'))"
                + "        OR (realm_id = 'phsa_aad' AND username NOT IN (SELECT username FROM keycloak.user_entity WHERE realm_id = 'phsa'))"
                + "       AND enabled = 1"
                + " )";

        return jdbcTemplate.queryForList(sql).get(0).get("TOTAL_USER_COUNT");
    }

    @GetMapping("/metrics/unique-user-count-by-idp")
    public List<Map<String, Object>> getUniqueUserCountByIDP() throws SQLException {
        String sql
                = "SELECT realm_id AS IDP, COUNT(1) AS UNIQUE_USER_COUNT"
                + "  FROM ("
                + "    SELECT DISTINCT id, realm_id"
                + "      FROM keycloak.user_entity"
                + "     WHERE LOWER(realm_id) IN ('bceid_basic', 'bceid_business', 'bcprovider_aad', 'bcsc', 'idir', 'idir_aad', 'moh_idp', 'phsa', 'phsa_aad')"
                + "       AND enabled = 1"
                + " )"
                + " GROUP BY realm_id"
                + " ORDER BY realm_id ASC";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/metrics/unique-user-count-by-realm")
    public List<Map<String, Object>> getUniqueUserCountByRealm() throws SQLException {
        String sql
                = "SELECT realm_id AS REALM, COUNT(1) AS UNIQUE_USER_COUNT"
                + "  FROM ("
                + "    SELECT DISTINCT id, realm_id"
                + "      FROM keycloak.user_entity"
                + "     WHERE LOWER(realm_id) NOT IN ('bceid_basic', 'bceid_business', 'bcprovider_aad', 'bcsc', 'idir', 'idir_aad', 'master', 'moh_idp', 'phsa', 'phsa_aad')"
                + "       AND enabled = 1"
                + " )"
                + " GROUP BY realm_id"
                + " ORDER BY realm_id ASC";

        return jdbcTemplate.queryForList(sql);
    }

}
