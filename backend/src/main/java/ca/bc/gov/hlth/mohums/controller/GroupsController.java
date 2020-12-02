package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GroupsController {

    private final WebClientService webClientService;

    public GroupsController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/groups")
    public ResponseEntity<Object> getGroups() {
        return webClientService.getGroups();
    }
}
