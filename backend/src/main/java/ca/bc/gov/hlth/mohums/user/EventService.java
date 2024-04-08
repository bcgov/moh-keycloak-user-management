package ca.bc.gov.hlth.mohums.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final EventRepository eventRepository;

    @Autowired
    public EventService(NamedParameterJdbcTemplate jdbcTemplate, EventRepository eventRepository) {
        this.namedParameterJdbcTemplate = jdbcTemplate;
        this.eventRepository = eventRepository;
    }

    public Map<String, String> getLastLoginEventsWithGivenClientAfterGivenDate(String lastLogAfter, String id) {
        String clientId = getClientIdById(id);
        long lastLogAfterEpoch = parseToEpochMilliseconds(lastLogAfter);
        return mapOf(eventRepository.findMohApplicationsLastLoginEventsWithGivenClientAfterGivenDate(lastLogAfterEpoch, clientId));
    }

    public Map<String, String> getLastLoginEventsAfterGivenDate(String lastLogAfter) {
        long lastLogAfterEpoch = parseToEpochMilliseconds(lastLogAfter);
        return mapOf(eventRepository.findMohApplicationsLastLoginEventsAfterGivenDate(lastLogAfterEpoch));
    }

    public Map<String, String> getLastLoginEventsWithGivenClientBeforeGivenDate(String lastLogBefore, String id) {
        List<String> usersWithoutLoginEvents = eventRepository.findMohApplicationUsersThatExistForOverAYearWithoutLoginEvents();
        Map<String, String> usersWithoutLoginEventsMap = new HashMap<>();
        long lastLogBeforeEpoch = parseToEpochMilliseconds(lastLogBefore);
        String clientId = getClientIdById(id);
        for (String userId : usersWithoutLoginEvents) {
            usersWithoutLoginEventsMap.put(userId, "Over a year ago");
        }
        Map<String, String> usersWithLastLoginBefore = mapOf(eventRepository.findMohApplicationsLastLoginEventsWithGivenClientBeforeGivenDate(lastLogBeforeEpoch, clientId));
        usersWithLastLoginBefore.putAll(usersWithoutLoginEventsMap);

        return usersWithLastLoginBefore;
    }

    public Map<String, String> getLastLoginEventsBeforeGivenDate(String lastLogBefore) {
        List<String> usersWithoutLoginEvents = eventRepository.findMohApplicationUsersThatExistForOverAYearWithoutLoginEvents();
        Map<String, String> usersWithoutLoginEventsMap = new HashMap<>();
        long lastLogBeforeEpoch = parseToEpochMilliseconds(lastLogBefore);
        for (String userId : usersWithoutLoginEvents) {
            usersWithoutLoginEventsMap.put(userId, "Over a year ago");
        }
        Map<String, String> usersWithLastLoginBefore = mapOf(eventRepository.findMohApplicationsLastLoginEventsBeforeGivenDate(lastLogBeforeEpoch));

        usersWithLastLoginBefore.putAll(usersWithoutLoginEventsMap);
        return usersWithLastLoginBefore;
    }

    private String getClientIdById(String id) {
        Map<String, String> namedParameters = Map.of("id", id);
        String sql = "SELECT c.client_id FROM keycloak.client c WHERE c.id = :id";
        return namedParameterJdbcTemplate.queryForObject(sql, namedParameters, String.class);
    }

    private Map<String, String> mapOf(List<LastLogDate> lastLogDates) {
        return lastLogDates.stream().collect(Collectors.toMap(LastLogDate::getUserId, lastLogDate -> lastLogDate.getLastLogin().toString()));
    }

    private long parseToEpochMilliseconds(String date){
        return LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
