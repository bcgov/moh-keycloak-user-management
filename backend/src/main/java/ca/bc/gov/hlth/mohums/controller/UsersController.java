package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.exceptions.HttpUnauthorizedException;
import ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser;
import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class UsersController {

    private final String usersPath = "/users";

    private final WebClientService webClientService;

    private final String vanityHostname;

    public UsersController(WebClientService webClientService, @Value("${config.vanity-hostname}") String vanityHostname) {
        this.webClientService = webClientService;
        this.vanityHostname = vanityHostname;
    }

    @GetMapping("/users")
    public Mono<Object> getUsers(
            @RequestParam Optional<Boolean> briefRepresentation,
            @RequestParam Optional<String> email,
            @RequestParam Optional<Integer> first,
            @RequestParam Optional<String> firstName,
            @RequestParam Optional<String> lastName,
            @RequestParam Optional<Integer> max,
            @RequestParam Optional<String> search,
            @RequestParam Optional<String> username
    ) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        briefRepresentation.ifPresent(briefRepresentationValue -> queryParams.add("briefRepresentation", briefRepresentationValue.toString()));
        email.ifPresent(emailValue -> queryParams.add("email", emailValue));
        first.ifPresent(firstValue -> queryParams.add("first", firstValue.toString()));
        firstName.ifPresent(firstNameValue -> queryParams.add("firstName", firstNameValue));
        lastName.ifPresent(lastNameValue -> queryParams.add("lastName", lastNameValue));
        max.ifPresent(maxValue -> queryParams.add("max", maxValue.toString()));
        search.ifPresent(searchValue -> queryParams.add("search", searchValue));
        username.ifPresent(usernameValue -> queryParams.add("username", usernameValue));

        return webClientService.get(usersPath, queryParams);
    }

    @GetMapping("/users/{userId}")
    public Mono<Object> getUser(@PathVariable String userId) {
        String path = usersPath + "/" + userId;
        return webClientService.get(path, null);
    }

    @PostMapping("/users")
    public Mono<ResponseEntity<Object>> createUser(@RequestBody Object body) {
        Mono<ClientResponse> post = webClientService.post(usersPath, body);
        return post.flatMap(response -> Mono.just(
                ResponseEntity.status(response.statusCode())
                        .headers(getHeaders(response.headers().asHttpHeaders()))
                        .body(response.bodyToMono(Object.class))));
    }

    @GetMapping("/users/{userId}/role-mappings/clients/{clientGuid}")
    public Mono<Object> getAssignedUserClientRoleMapping(@RequestHeader("Authorization") String token, @PathVariable String userId, @PathVariable String clientGuid) {

        //Check if the user has the required role to view information related to this client
        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.getAssignedUserClientRoleMappings(userId, clientGuid);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to view user details for this client");
        }
    }

    @GetMapping("/users/{userId}/role-mappings/clients/{clientGuid}/available")
    public Mono<Object> getAvailableUserClientRoleMapping(@RequestHeader("Authorization") String token, @PathVariable String userId, @PathVariable String clientGuid) {

        //Check if the user has the required role to view information related to this client
        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.getAvailableUserClientRoleMappings(userId, clientGuid);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to view user details for this client");
        }
    }

    @GetMapping("/users/{userId}/role-mappings/clients/{clientGuid}/composite")
    public Mono<Object> getEffectiveUserClientRoleMapping(@RequestHeader("Authorization") String token, @PathVariable String userId, @PathVariable String clientGuid) {

        //Check if the user has the required role to view information related to this client
        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.getEffectiveUserClientRoleMappings(userId, clientGuid);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to view user details for this client");
        }
    }

    private static final Pattern patternGuid = Pattern.compile(".*/users/(.{8}-.{4}-.{4}-.{4}-.{12})");

    HttpHeaders getHeaders(HttpHeaders response) {

        // If no Location header found, return empty Headers.
        HttpHeaders newHeaders = new HttpHeaders();
        if (response == null || response.getLocation() == null) {
            return newHeaders;
        }

        // If Location header found, replace Keycloak hostname with service vanity hostname.
        Matcher matcher = patternGuid.matcher(response.getLocation().toASCIIString());
        if (matcher.matches() && matcher.groupCount() == 1) {
            newHeaders.setLocation(URI.create("https://" + vanityHostname + "/users/" + matcher.group(1)));
        }

        return newHeaders;
    }

    /* This method checks the client guid from the request against the users roles
    * Since the roles match by Client ID and the request uses the guid we need to do a lookup against keycloak to get the
    * Client ID*/
    boolean isAuthorizedToViewClient(String token, String clientGuid) {
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parse(token);

        Object authFilteredClient = webClientService.getClient(clientGuid)
                .filter(c -> Objects.nonNull(((LinkedHashMap) c).get("clientId")))
                .filter(c -> authorizedClients.contains(((LinkedHashMap) c).get("clientId").toString().toLowerCase()))
                .block();

        return authFilteredClient != null;
    }

}
