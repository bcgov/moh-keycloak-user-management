package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventsController {

    private final WebClientService webClientService;

    public EventsController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/events")
    public ResponseEntity<Object> getEvents(@RequestParam MultiValueMap<String, String> allParams) {
        return webClientService.getEvents(allParams);
    }
}
