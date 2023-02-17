package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.KeycloakApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GroupsController {

    private final KeycloakApiService keycloakApiService;

    public GroupsController(KeycloakApiService keycloakApiService) {
        this.keycloakApiService = keycloakApiService;
    }

    @GetMapping("/groups")
    public ResponseEntity<Object> getGroups() {
        return keycloakApiService.getGroups();
    }

}
