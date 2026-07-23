package ca.bc.gov.hlth.mohums.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class MetricsServiceTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final MetricsService metricsService = new MetricsService(jdbcTemplate);

    @Test
    void metricsAreEmptyBeforeCacheRefresh() {
        assertEquals(List.of(), metricsService.getActiveUserCount());
        assertEquals(List.of(), metricsService.getTotalActiveUserCountYear());
        assertNull(metricsService.getTotalNumberOfUsers());
        assertEquals(List.of(), metricsService.getUniqueUserCountByIDP());
        assertEquals(List.of(), metricsService.getUniqueUserCountByRealm());
    }

    @Test
    void refreshMetricsCachesQueryResults() {
        mockSuccessfulMetricQueries();

        metricsService.refreshMetrics();

        assertEquals(List.of(row("REALM", "moh_applications")), metricsService.getActiveUserCount());
        assertEquals(List.of(row("ACTIVE_USER_COUNT", 10)), metricsService.getTotalActiveUserCountYear());
        assertEquals(20, metricsService.getTotalNumberOfUsers());
        assertEquals(List.of(row("IDP", "idir_aad")), metricsService.getUniqueUserCountByIDP());
        assertEquals(List.of(row("REALM", "moh_citizen")), metricsService.getUniqueUserCountByRealm());
        assertThrows(UnsupportedOperationException.class,
                () -> metricsService.getActiveUserCount().get(0).put("REALM", "changed"));
    }

    @Test
    void refreshMetricsKeepsPreviousCacheWhenQueriesFail() {
        mockSuccessfulMetricQueries();
        metricsService.refreshMetrics();

        mockFailedMetricQueries();
        metricsService.refreshMetrics();

        assertEquals(List.of(row("REALM", "moh_applications")), metricsService.getActiveUserCount());
        assertEquals(List.of(row("ACTIVE_USER_COUNT", 10)), metricsService.getTotalActiveUserCountYear());
        assertEquals(20, metricsService.getTotalNumberOfUsers());
        assertEquals(List.of(row("IDP", "idir_aad")), metricsService.getUniqueUserCountByIDP());
        assertEquals(List.of(row("REALM", "moh_citizen")), metricsService.getUniqueUserCountByRealm());
    }

    private void mockSuccessfulMetricQueries() {
        when(jdbcTemplate.queryForList(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);

            // Active user count by realm and client.
            if (sql.contains("ee.realm_id AS realm")) {
                return List.of(row("REALM", "moh_applications"));
            }

            // Total active user count by day over the last year.
            if (sql.contains("EVENT_DATE")) {
                return List.of(row("ACTIVE_USER_COUNT", 10));
            }

            // Total active user count across non-internal clients.
            if (sql.contains("TOTAL_USER_COUNT")) {
                return List.of(row("TOTAL_USER_COUNT", 20));
            }

            // Unique enabled user count grouped by identity provider realm.
            if (sql.contains("AS IDP")) {
                return List.of(row("IDP", "idir_aad"));
            }

            // Unique enabled user count grouped by application realm.
            if (sql.contains("AS REALM")) {
                return List.of(row("REALM", "moh_citizen"));
            }

            throw new IllegalArgumentException("Unexpected metrics query: " + sql);
        });
    }

    private void mockFailedMetricQueries() {
        doThrow(new RuntimeException("database unavailable")).when(jdbcTemplate).queryForList(anyString());
    }

    private Map<String, Object> row(String key, Object value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(key, value);
        return row;
    }

}
