package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GroupsController {

    private final WebClientService webClientService;

    public GroupsController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/groups")
    public Mono<Object> groups() {
        String groupsPath = "/groups";
        return webClientService.get(groupsPath, null);
    }
}
