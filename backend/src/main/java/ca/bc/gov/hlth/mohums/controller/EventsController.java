package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.KeycloakApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventsController {

    private final KeycloakApiService keycloakApiService;

    public EventsController(KeycloakApiService keycloakApiService) {
        this.keycloakApiService = keycloakApiService;
    }

    @GetMapping("/events")
    public ResponseEntity<Object> getEvents(@RequestParam MultiValueMap<String, String> allParams) {
        return keycloakApiService.getEvents(allParams);
    }

    @GetMapping("/admin-events")
    public ResponseEntity<Object> getAdminEvents(@RequestParam MultiValueMap<String, String> allParams) {
        return keycloakApiService.getAdminEvents(allParams);
    }
}
