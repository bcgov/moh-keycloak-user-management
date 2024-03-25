package ca.bc.gov.hlth.mohums.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

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

    public Map<String, String> getLastLoginEventsWithGivenClientAfterGivenDate(long lastLogAfterEpoch, String id) {
        String clientId = getClientIdById(id);
        return mapOf(eventRepository.findMohApplicationsLastLoginEventsWithGivenClientAfterGivenDate(lastLogAfterEpoch, clientId));
    }

    public Map<String, String> getLastLoginEventsAfterGivenDate(long lastLogAfterEpoch) {
        return mapOf(eventRepository.findMohApplicationsLastLoginEventsAfterGivenDate(lastLogAfterEpoch));
    }

    private String getClientIdById(String id) {
        Map<String, String> namedParameters = Map.of("id", id);
        String sql = "SELECT c.client_id FROM keycloak.client c WHERE c.id = :id";
        return namedParameterJdbcTemplate.queryForObject(sql, namedParameters, String.class);
    }

    private Map<String, String> mapOf(List<LastLogDate> lastLogDates){
        return lastLogDates.stream().collect(Collectors.toMap(LastLogDate::getUserId, lastLogDate -> lastLogDate.getLastLogin().toString()));
    }
}
