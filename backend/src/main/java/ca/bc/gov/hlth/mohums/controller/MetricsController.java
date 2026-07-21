package ca.bc.gov.hlth.mohums.controller;

import java.util.List;
import java.util.Map;

import ca.bc.gov.hlth.mohums.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/metrics/active-user-count")
    public List<Map<String, Object>> getActiveUserCount() {
        return metricsService.getActiveUserCount();
    }

    @GetMapping("/metrics/total-active-user-count")
    public List<Map<String, Object>> getTotalActiveUserCountYear() {
        return metricsService.getTotalActiveUserCountYear();
    }

    @GetMapping("/metrics/total-number-of-users")
    public Object getTotalNumberOfUsers() {
        return metricsService.getTotalNumberOfUsers();
    }

    @GetMapping("/metrics/unique-user-count-by-idp")
    public List<Map<String, Object>> getUniqueUserCountByIDP() {
        return metricsService.getUniqueUserCountByIDP();
    }

    @GetMapping("/metrics/unique-user-count-by-realm")
    public List<Map<String, Object>> getUniqueUserCountByRealm() {
        return metricsService.getUniqueUserCountByRealm();
    }

}
