package ca.bc.gov.hlth.mohums.group;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GroupController {

    @Autowired
    private WebClientService webClientService;

    @GetMapping("/groups")
    public Mono<Object> groups() {
        return webClientService.getGroups();
    }
}
