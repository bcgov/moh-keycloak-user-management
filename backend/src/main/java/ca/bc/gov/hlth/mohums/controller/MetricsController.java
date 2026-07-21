package ca.bc.gov.hlth.mohums.controller;

import java.util.List;
import java.util.Map;
import java.sql.*;

import ca.bc.gov.hlth.mohums.service.MetricsService;
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

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/metrics/active-user-count")
    public List<Map<String, Object>> getActiveUserCount() throws SQLException {
        return metricsService.getActiveUserCount();
    }

    @GetMapping("/metrics/total-active-user-count")
    public List<Map<String, Object>> getTotalActiveUserCountYear() throws SQLException {
        String sql
                = "SELECT EVENT_DATE, COUNT(1) AS ACTIVE_USER_COUNT"
                + "  FROM ("
                + "    SELECT TRUNC(FROM_TZ(CAST(to_date('19700101', 'YYYYMMDD') + NUMTODSINTERVAL(event_time/1000, 'SECOND') AS timestamp), 'UTC') AT TIME ZONE 'America/Vancouver') AS EVENT_DATE"
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
        String sql = """
                SELECT COUNT(DISTINCT ue.id) AS TOTAL_USER_COUNT
                FROM keycloak.user_entity ue
                JOIN keycloak.realm r ON ue.realm_id = r.id
                WHERE ue.enabled = 1
                  AND ue.service_account_client_link IS NULL
                  AND LOWER(r.name) IN (
                      'moh_applications',
                      'moh_citizen',
                      'mhsu_foundry',
                      'bcer',
                      'bcerd'
                  )
                """;

        return jdbcTemplate.queryForList(sql).get(0).get("TOTAL_USER_COUNT");
    }

    @GetMapping("/metrics/unique-user-count-by-idp")
    public List<Map<String, Object>> getUniqueUserCountByIDP() throws SQLException {
        String sql = """
                SELECT r.name AS IDP,
                       COUNT(DISTINCT ue.id) AS UNIQUE_USER_COUNT
                FROM keycloak.user_entity ue
                JOIN keycloak.realm r
                    ON ue.realm_id = r.id
                WHERE ue.enabled = 1
                  AND ue.service_account_client_link IS NULL
                  AND LOWER(r.name) NOT IN (
                      'moh_applications',
                      'moh_citizen',
                      'mhsu_foundry',
                      'bcer',
                      'bcerd',
                      'v2_pos',
                      'master'
                  )
                GROUP BY r.name
                ORDER BY r.name ASC
                """;

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/metrics/unique-user-count-by-realm")
    public List<Map<String, Object>> getUniqueUserCountByRealm() throws SQLException {
        String sql = """
                SELECT r.name AS REALM,
                       COUNT(DISTINCT ue.id) AS UNIQUE_USER_COUNT
                FROM keycloak.user_entity ue
                JOIN keycloak.realm r
                    ON ue.realm_id = r.id
                WHERE ue.enabled = 1
                  AND ue.service_account_client_link IS NULL
                  AND LOWER(r.name) IN (
                      'moh_applications',
                      'moh_citizen',
                      'mhsu_foundry',
                      'bcer',
                      'bcerd'
                  )
                GROUP BY r.name
                ORDER BY r.name ASC
                """;

        return jdbcTemplate.queryForList(sql);
    }

}
