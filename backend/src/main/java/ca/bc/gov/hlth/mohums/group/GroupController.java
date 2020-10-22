package ca.bc.gov.hlth.mohums.group;

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

    @GetMapping("/groups")
    public Mono<Object> groups() {
        return webClientService.getGroups();
    }
}
