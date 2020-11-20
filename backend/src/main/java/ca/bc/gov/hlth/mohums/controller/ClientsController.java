package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser;
import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class ClientsController {

    private final WebClientService webClientService;

    public ClientsController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/clients")
    public Mono<Object> clients(@RequestHeader("Authorization") String token) {
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parse(token);
        return webClientService.getClients()
                .filter(c -> authorizedClients.contains(((LinkedHashMap) c).get("clientId").toString().toLowerCase()))
                .collectList()
                .cast(Object.class);
    }
}
