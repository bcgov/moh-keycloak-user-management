package ca.bc.gov.hlth.mohums.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final JdbcTemplate jdbcTemplate;
    private final ReentrantLock metricsRefreshLock = new ReentrantLock();
    private volatile List<Map<String, Object>> activeUserCount = List.of();
    private volatile List<Map<String, Object>> totalActiveUserCountYear = List.of();
    private volatile Object totalNumberOfUsers;
    private volatile List<Map<String, Object>> uniqueUserCountByIDP = List.of();
    private volatile List<Map<String, Object>> uniqueUserCountByRealm = List.of();

    public MetricsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getActiveUserCount() {
        return activeUserCount;
    }

    public List<Map<String, Object>> getTotalActiveUserCountYear() {
        return totalActiveUserCountYear;
    }

    public Object getTotalNumberOfUsers() {
        return totalNumberOfUsers;
    }

    public List<Map<String, Object>> getUniqueUserCountByIDP() {
        return uniqueUserCountByIDP;
    }

    public List<Map<String, Object>> getUniqueUserCountByRealm() {
        return uniqueUserCountByRealm;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshMetricsOnStartup() {
        refreshMetrics();
    }

    @Scheduled(fixedRateString = "${metrics.cache-refresh-ms:${metrics.active-user-count.cache-refresh-ms:3600000}}")
    public void refreshMetrics() {
        if (!metricsRefreshLock.tryLock()) {
            logger.info("Metrics cache refresh is already running; skipping this refresh.");
            return;
        }

        try {
            logger.info("Refreshing metrics cache.");
            refreshMetric("active user count", () -> activeUserCount = queryActiveUserCount());
            refreshMetric("total active user count year", () -> totalActiveUserCountYear = queryTotalActiveUserCountYear());
            refreshMetric("total number of users", () -> totalNumberOfUsers = queryTotalNumberOfUsers());
            refreshMetric("unique user count by IDP", () -> uniqueUserCountByIDP = queryUniqueUserCountByIDP());
            refreshMetric("unique user count by realm", () -> uniqueUserCountByRealm = queryUniqueUserCountByRealm());
            logger.info("Metrics cache refresh complete.");
        } finally {
            metricsRefreshLock.unlock();
        }
    }

    private void refreshMetric(String metricName, Supplier<Object> refresh) {
        try {
            Object result = refresh.get();
            if (result instanceof List<?> rows) {
                logger.info("Refreshed {} cache with {} rows.", metricName, rows.size());
            } else {
                logger.info("Refreshed {} cache.", metricName);
            }
        } catch (Exception e) {
            logger.error("Failed to refresh {} cache. Keeping the previous cached value.", metricName, e);
        }
    }

    private List<Map<String, Object>> queryActiveUserCount() {
        String sql = """
                SELECT
                    ee.realm_id AS realm,
                    ee.client_id AS client,
                    COUNT(DISTINCT ee.user_id) AS active_user_count,
                    c.description AS description
                FROM keycloak.event_entity ee
                JOIN keycloak.user_entity ue
                  ON ue.id = ee.user_id
                 AND ue.enabled = 1
                JOIN keycloak.client c
                  ON c.client_id = ee.client_id
                 AND c.realm_id = ee.realm_id
                WHERE ee.type = 'LOGIN'
                  AND ee.event_time > (
                      SYSDATE - 365 - DATE '1970-01-01'
                  ) * 24 * 60 * 60 * 1000
                  AND NOT (
                         c.client_id IN (
                             'account',
                             'account-console',
                             'security-admin-console',
                             'JAVASCRIPT_CONSOLE'
                         )
                      OR LOWER(c.client_id) LIKE '%realm%'
                  )
                GROUP BY
                    ee.realm_id,
                    ee.client_id,
                    c.description
                ORDER BY
                    ee.realm_id,
                    ee.client_id
                """;

        return cacheRows(jdbcTemplate.queryForList(sql));
    }

    private List<Map<String, Object>> queryTotalActiveUserCountYear() {
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

        return cacheRows(jdbcTemplate.queryForList(sql));
    }

    private Object queryTotalNumberOfUsers() {
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

    private List<Map<String, Object>> queryUniqueUserCountByIDP() {
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

        return cacheRows(jdbcTemplate.queryForList(sql));
    }

    private List<Map<String, Object>> queryUniqueUserCountByRealm() {
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

        return cacheRows(jdbcTemplate.queryForList(sql));
    }

    private List<Map<String, Object>> cacheRows(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> Collections.unmodifiableMap(new LinkedHashMap<>(row)))
                .toList();
    }
}
