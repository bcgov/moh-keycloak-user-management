package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser;
import ca.bc.gov.hlth.mohums.webclient.KeycloakApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ClientsController {

    private final KeycloakApiService keycloakApiService;

    public ClientsController(KeycloakApiService keycloakApiService) {
        this.keycloakApiService = keycloakApiService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<Object>> getClients(@RequestHeader("Authorization") String token) {
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parse(token);
        ResponseEntity<List<Object>> clients = keycloakApiService.getClients();
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
        return keycloakApiService.getClientRoles(clientId);
    }

    @GetMapping("/clients/{clientId}/roles/{roleName}/users")
    public ResponseEntity<List<Object>> getUsersInRole(
            @PathVariable String clientId,
            @PathVariable String roleName,
            @RequestParam Optional<Boolean> briefRepresentation,
            @RequestParam Optional<Integer> first,
            @RequestParam Optional<Integer> max
    ) {
        MultiValueMap<String, String> queryParams = prepareQueryParams(briefRepresentation, first, max);

        return keycloakApiService.getUsersInRole(clientId, roleName, queryParams);
    }

    /**
     * This endpoint lets you fetch users from a given realm that have assigned a specified role
     * @param realm - name of the realm
     * @param clientId - id (GUID) of the client
     * @param roleName - name of the role
     * The function will return a list of users that specify the criteria.
     * The function will return 404 if realm/client/role cannot be found.
     * The actual call to Keycloak API is executed by the service account in master realm. Minimal viable permissions for that service account are:
     *                 view-clients, view-users, query-clients, query-users roles.
     */
    @GetMapping("/{realm}/clients/{clientId}/roles/{roleName}/users")
    public ResponseEntity<List<Object>> getUsersInRoleAnyRealm(
            @PathVariable String realm,
            @PathVariable String clientId,
            @PathVariable String roleName,
            @RequestParam Optional<Boolean> briefRepresentation,
            @RequestParam Optional<Integer> first,
            @RequestParam Optional<Integer> max
    ) {
        MultiValueMap<String, String> queryParams = prepareQueryParams(briefRepresentation, first, max);

        return keycloakApiService.getUsersInRoleInGivenRealm(realm, clientId, roleName, queryParams);
    }

    private MultiValueMap<String, String> prepareQueryParams(Optional<Boolean> briefRepresentation, Optional<Integer> first, Optional<Integer> max){
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        briefRepresentation.ifPresent(briefRepresentationValue -> queryParams.add("briefRepresentation", briefRepresentationValue.toString()));
        first.ifPresent(firstValue -> queryParams.add("first", firstValue.toString()));
        max.ifPresent(maxValue -> queryParams.add("max", maxValue.toString()));

        return queryParams;
    }

}
