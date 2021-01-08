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

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class UsersController {

    private final WebClientService webClientService;

    private final String vanityHostname;

    public UsersController(WebClientService webClientService, @Value("${config.vanity-hostname}") String vanityHostname) {
        this.webClientService = webClientService;
        this.vanityHostname = vanityHostname;
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getUsers(
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

        return webClientService.getUsers(queryParams);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable String userId) {
        return webClientService.getUser(userId);
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestBody Object body) {
        ResponseEntity<Object> post = webClientService.createUser(body);
        return ResponseEntity.status(post.getStatusCode())
                .headers(convertLocationHeader(post.getHeaders()))
                .body(post.getBody());
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable String userId, @RequestBody Object body) {
        ResponseEntity<Object> post = webClientService.updateUser(userId, body);
        return ResponseEntity.status(post.getStatusCode())
                .headers(post.getHeaders())
                .body(post.getBody());
    }

    @GetMapping("/users/{userId}/role-mappings/clients/{clientGuid}")
    public ResponseEntity<Object> getAssignedUserClientRoleMapping(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId,
            @PathVariable String clientGuid) {

        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.getAssignedUserClientRoleMappings(userId, clientGuid);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to view user details for this client");
        }
    }

    @GetMapping("/users/{userId}/role-mappings/clients/{clientGuid}/available")
    public ResponseEntity<Object> getAvailableUserClientRoleMapping(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId,
            @PathVariable String clientGuid) {

        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.getAvailableUserClientRoleMappings(userId, clientGuid);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to view user details for this client");
        }
    }

    @GetMapping("/users/{userId}/role-mappings/clients/{clientGuid}/composite")
    public ResponseEntity<Object> getEffectiveUserClientRoleMapping(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId,
            @PathVariable String clientGuid) {

        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.getEffectiveUserClientRoleMappings(userId, clientGuid);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to view user details for this client");
        }
    }

    @PostMapping("/users/{userId}/role-mappings/clients/{clientGuid}")
    public ResponseEntity<Object> addUserClientRoles(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId,
            @PathVariable String clientGuid,
            @RequestBody Object body) {
        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.addUserClientRole(userId, clientGuid, body);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to update user details for this client");
        }
    }

    @DeleteMapping("/users/{userId}/role-mappings/clients/{clientGuid}")
    public ResponseEntity<Object> deleteUserClientRoles(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId,
            @PathVariable String clientGuid,
            @RequestBody Object body) {
        if (isAuthorizedToViewClient(token, clientGuid)) {
            return webClientService.deleteUserClientRole(userId, clientGuid, body);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to update user details for this client");
        }
    }

    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<Object> getUserGroups(@PathVariable String userId) {
        return webClientService.getUserGroups(userId);
    }

    @PutMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Object> addUserGroups(@PathVariable String userId,
                                                @PathVariable String groupId) {
        return webClientService.addUserGroups(userId, groupId);
    }

    @DeleteMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Object> removeUserGroups(@PathVariable String userId,
                                                @PathVariable String groupId) {
        return webClientService.removeUserGroups(userId, groupId);
    }

    private static final Pattern patternGuid = Pattern.compile(".*/users/(.{8}-.{4}-.{4}-.{4}-.{12})");

    /**
     * Convert Keycloak's Location header to this service's Location header. Only handles Locations
     * containing the "users" path.
     *
     * e.g. https://common-logon.hlth.gov.bc.ca/users/lknlnlkn becomes https://servicename/users/lknlnlkn.
     * Other headers are left untouched.
     *
     * @param inHeaders the headers from Keycloak.
     * @return headers with a possibly modified Location header.
     */
    HttpHeaders convertLocationHeader(HttpHeaders inHeaders) {
        Objects.requireNonNull(inHeaders);

        // The inHeaders are read-only, so they must be copied to be modified.
        HttpHeaders outHeaders = HttpHeaders.writableHttpHeaders(inHeaders);

        // If no Location header, return headers unmodified.
        if (inHeaders.getLocation() == null) {
            return outHeaders;
        }

        // If Location header present, replace Keycloak hostname with service vanity hostname.
        Matcher matcher = patternGuid.matcher(inHeaders.getLocation().toASCIIString());
        if (matcher.matches() && matcher.groupCount() == 1) {
            outHeaders.setLocation(URI.create(vanityHostname + "/users/" + matcher.group(1)));
        }

        return outHeaders;
    }

    /**
     * Checks the client GUID from the request against the user's roles. Since the roles match by Client ID
     * and the request uses the GUID, we need to do a lookup against Keycloak to get the Client ID.
     */
    boolean isAuthorizedToViewClient(String token, String clientGuid) {
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parse(token);

        LinkedHashMap<String, String> client = (LinkedHashMap<String, String>) webClientService.getClient(clientGuid).getBody();
        // TODO If the client doesn't exist return a 401 (should this be a 404 which is the actual KC response)
        if (client.get("clientId") != null) {
            return authorizedClients.contains(client.get("clientId").toLowerCase());
        } else {
            return false;
        }

    }

}
