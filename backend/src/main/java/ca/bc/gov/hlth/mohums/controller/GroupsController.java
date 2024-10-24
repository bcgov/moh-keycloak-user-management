package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.KeycloakApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class GroupsController {

    private final KeycloakApiService keycloakApiService;

    public GroupsController(KeycloakApiService keycloakApiService) {
        this.keycloakApiService = keycloakApiService;
    }

    @GetMapping("")
    public ResponseEntity<Object> getGroups() {
        return keycloakApiService.getGroups();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<Object> getGroupMembers(@PathVariable String groupId) {
        return keycloakApiService.getGroupMembers(groupId);
    }

}
