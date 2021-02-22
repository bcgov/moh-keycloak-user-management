package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser;
import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientsController {

    private final WebClientService webClientService;

    public ClientsController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<Object>> getClients(@RequestHeader("Authorization") String token) {
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parse(token);
        ResponseEntity<List<Object>> clients = webClientService.getClients();
        return ResponseEntity.status(clients.getStatusCode())
                .headers(clients.getHeaders())
                .body(clients.getBody().stream()
                        .filter(c -> authorizedClients.contains(((LinkedHashMap) c).get("clientId").toString().toLowerCase()))
                        .collect(Collectors.toList()));
    }

    @GetMapping("/clients/{clientId}/roles")
    public ResponseEntity<List<Object>> getClientRoles(
            @PathVariable String clientId
    ) {
        return webClientService.getClientRoles(clientId);
    }

    @GetMapping("/clients/{clientId}/roles/{roleName}/users")
    public ResponseEntity<List<Object>> getUsersInRole(
            @PathVariable String clientId,
            @PathVariable String roleName
    ) {
        return webClientService.getUsersInRole(clientId, roleName);
    }

}
