package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GroupController {

    private final WebClientService webClientService;

    public GroupController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    private final String groupsPath = "/groups";

    @GetMapping("/groups")
    public Mono<Object> groups() {
        return webClientService.get(groupsPath, null);
    }
}
