package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.exceptions.BulkRemovalRequestException;
import ca.bc.gov.hlth.mohums.exceptions.HttpUnauthorizedException;
import ca.bc.gov.hlth.mohums.model.BulkRemovalRequest;
import ca.bc.gov.hlth.mohums.model.BulkRemovalResponse;
import ca.bc.gov.hlth.mohums.model.UserPayee;
import ca.bc.gov.hlth.mohums.userSearch.user.UserDTO;
import ca.bc.gov.hlth.mohums.userSearch.user.UserSearchParameters;
import ca.bc.gov.hlth.mohums.userSearch.user.UserService;
import ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser;
import ca.bc.gov.hlth.mohums.validator.BulkRemovalRequestValidator;
import ca.bc.gov.hlth.mohums.validator.PermissionsValidator;
import ca.bc.gov.hlth.mohums.webclient.KeycloakApiService;
import ca.bc.gov.hlth.mohums.webclient.PayeeApiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
public class UsersController {

    private static final String KEY_PAYEE_NUMBER = "payeeNumber";

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private PermissionsValidator permissionsValidator;

    private final BulkRemovalRequestValidator bulkRemovalRequestValidator = new BulkRemovalRequestValidator();

    private final KeycloakApiService keycloakApiService;

    private final PayeeApiService payeeApiService;

    private final String vanityHostname;

    @Autowired
    private final UserService userService;

    public UsersController(KeycloakApiService keycloakApiService, PayeeApiService payeeApiService, @Value("${config.vanity-hostname}") String vanityHostname, UserService userService) {
        this.keycloakApiService = keycloakApiService;
        this.payeeApiService = payeeApiService;
        this.vanityHostname = vanityHostname;
        this.userService = userService;
    }

    /**
     * @param briefRepresentation indicates if a brief representation of the client should be returned by the Keycloak API get Users
     * @param email               the email of the user
     * @param firstName           the first name of the user
     * @param lastName            the last name of the user
     * @param search              a String contained in username, first or last name, or email of the user
     * @param username            the username of the user
     * @param org                 the ID of an Organization the user is associated to
     * @param lastLogAfter        the date after which the user logged in
     * @param lastLogBefore       the date before which the user logged in
     * @param clientId            the Id, which is a UUID, of the Client entity, maps to keycloak.client.id in the keycloak database model
     * @param selectedRoles       the roles the user must have, an empty list means all role for that client will be used
     * @return a list of Users that match the specified criteria
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam Optional<Boolean> briefRepresentation,
            @RequestParam Optional<String> email,
            @RequestParam Optional<String> firstName,
            @RequestParam Optional<String> lastName,
            @RequestParam Optional<String> search,
            @RequestParam Optional<String> username,
            @RequestParam Optional<String> org,
            @RequestParam Optional<String> lastLogAfter,
            @RequestParam Optional<String> lastLogBefore,
            @RequestParam Optional<String> clientId,
            @RequestParam Optional<String[]> selectedRoles) {

        UserSearchParameters params = new UserSearchParameters(briefRepresentation, email, firstName, lastName, search, username, org, clientId, selectedRoles, lastLogAfter, lastLogBefore);
        List<String> errors = params.validateParameters();
        if(!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors.stream().collect(Collectors.joining()));
        }
        List<UserDTO> a = userService.getUsers(params);
        return ResponseEntity.ok(a);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable String userId) {
        return keycloakApiService.getUser(userId);
    }

    @PostMapping("/users")
    public ResponseEntity<Object> createUser(@RequestBody Object body) {
        ResponseEntity<Object> post = keycloakApiService.createUser(body);
        return ResponseEntity.status(post.getStatusCode())
                .headers(convertLocationHeader(post.getHeaders()))
                .body(post.getBody());
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable String userId, @RequestBody Object body) {
        ResponseEntity<Object> post = keycloakApiService.updateUser(userId, body);
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
            return keycloakApiService.getAssignedUserClientRoleMappings(userId, clientGuid);
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
            return keycloakApiService.getAvailableUserClientRoleMappings(userId, clientGuid);
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
            return keycloakApiService.getEffectiveUserClientRoleMappings(userId, clientGuid);
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
        if (isAuthorizedToEditClient(token, clientGuid)) {
            return keycloakApiService.addUserClientRole(userId, clientGuid, body);
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
        if (isAuthorizedToEditClient(token, clientGuid)) {
            return keycloakApiService.deleteUserClientRole(userId, clientGuid, body);
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to update user details for this client");
        }
    }

    /**
     *
     * @param token - bulk-removal role required
     * @param clientGuid - id of a client, container of the roles that will be unassigned from users
     * @param removalRequest - map of user id and list of roles to be unassigned from the user
     * @return list of responses from Keycloak API with corresponding status codes
     *
     * Firstly, requester permissions and validity of client id are checked
     * Secondly, the removal request is being validated. It is a required parameter. It must contain user id and a non-empty list of roles to be unassigned
     * For each map entry, the method calls Keycloak API which revokes roles from the user. Those calls are not transactional - one can fail, but other can be processed.
     *
     * In case of a success the Keycloak API call returns a 204 status (NO_CONTENT)
     * If user id from the request body cannot be associated with a Keycloak user, the Keycloak API will return 404 status (NOT_FOUND)
     * If role id from the request body cannot be associated with a Keycloak user, the Keycloak API will return 404 status (NOT_FOUND).
     * If at least one of the roles from the map entry is invalid, the Keycloak API call for this entry fails.
     */
    @DeleteMapping("/bulk-removal/{clientGuid}")
    public ResponseEntity<List<BulkRemovalResponse>> bulkRemoveUserClientRoles(
            @RequestHeader("Authorization") String token,
            @PathVariable String clientGuid,
            @RequestBody BulkRemovalRequest removalRequest) {
        if (isAuthorizedToEditClient(token, clientGuid)) {
            Optional<String> removalRequestValidationError = bulkRemovalRequestValidator.validateBulkRemovalRequest(removalRequest);
            removalRequestValidationError.ifPresent(message -> {
                throw new BulkRemovalRequestException(message);
            });
            return ResponseEntity.ok(keycloakApiService.bulkRemoveUserClientRoles(clientGuid, removalRequest));
        } else {
            throw new HttpUnauthorizedException("Token does not have a valid role to update user details for this client");
        }
    }

    @GetMapping("/users/{userId}/last-logins")
    public ResponseEntity<Object> getUserLogins(@PathVariable String userId) {
        int maxRecords = 5000;
        LocalDate oneYearAgo = LocalDate.now().minus(1, ChronoUnit.YEARS);
        Optional<String> oneYearAgoParam = Optional.of(oneYearAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        MultiValueMap<String, String> params = buildQueryEventsByUser(userId, 0, maxRecords, oneYearAgoParam, Optional.empty());
        List<Map<String, Object>> logins = (List<Map<String, Object>>) keycloakApiService.getEvents(params).getBody();

        Map<String, Long> mostRecentLogins = new HashMap<>();
        for (Map<String, Object> login : logins) {
            String clientId = (String) login.get("clientId");
            Long time = (Long) login.get("time");
            if (!mostRecentLogins.containsKey(clientId) || time > mostRecentLogins.get(clientId)) {
                mostRecentLogins.put(clientId, time);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(mostRecentLogins);
    }

    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<Object> getUserGroups(@PathVariable String userId) {
        return keycloakApiService.getUserGroups(userId);
    }

    @PutMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Object> addUserGroups(@PathVariable String userId,
                                                @PathVariable String groupId,
                                                @RequestBody String groupName,
                                                @AuthenticationPrincipal Jwt jwt) {
        return permissionsValidator.validateGroupManagementPermission(jwt, groupName) ?
                keycloakApiService.addUserGroups(userId, groupId) : ResponseEntity.status(HttpStatus.FORBIDDEN).body("Add user to group - permission denied");
    }

    @DeleteMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Object> removeUserGroups(@PathVariable String userId,
                                                   @PathVariable String groupId,
                                                   @RequestBody String groupName,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return permissionsValidator.validateGroupManagementPermission(jwt, groupName) ?
                keycloakApiService.removeUserGroups(userId, groupId) : ResponseEntity.status(HttpStatus.FORBIDDEN).body("Remove user from group - permission denied");
    }

    @DeleteMapping("/users/{userId}/federated-identity/{identityProvider}")
    public ResponseEntity<Object> removeUserIdentityProviderLinks(@PathVariable String userId, @PathVariable String identityProvider, @RequestBody String userIdIdpRealm) {
        return keycloakApiService.removeUserIdentityProviderLink(userId, identityProvider, userIdIdpRealm);
    }

    @GetMapping("/users/{userId}/payee")
    public ResponseEntity<Object> getUserPayee(@PathVariable String userId) {
        try {
            ResponseEntity<Object> response = payeeApiService.getPayee(userId);
            // A 404 is a legitimate response if the user doesn't have a Payee defined
            // Convert it and return an empty response instead.
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.ok(null);
            }
            // Create a new Response as returning the original response creates intermittent
            // network errors for the client 
            return new ResponseEntity<Object>(response.getBody(), response.getStatusCode());
        } catch (Exception e) {
            // WebFlux will thrown an exception if the Body isn't as expected (which can happen with a Payee API exception)
            // so catch and return a 500
            return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @SuppressWarnings("unchecked")
    @PutMapping("/users/{userId}/payee")
    public ResponseEntity<Object> updateUserPayee(@PathVariable String userId, @RequestBody(required = false) String payee) {
        // Get the existing payee
        String existingPayee = ((LinkedHashMap<String, String>) payeeApiService.getPayee(userId).getBody()).get(KEY_PAYEE_NUMBER);

        // No change - do nothing
        if (StringUtils.equals(payee, existingPayee)) {
            return ResponseEntity.ok(payee);
        }
        if (StringUtils.isEmpty(existingPayee)) {
            if (StringUtils.isNotEmpty(payee)) {
                // Add a new record
                UserPayee userPayee = new UserPayee();
                userPayee.setPayeeNumber(payee);
                userPayee.setUserGuid(userId);
                return payeeApiService.addPayee(userPayee);
            } else {
                // Technically this won't happen as this would be "no change"
                return ResponseEntity.ok(payee);
            }
        } else {
            if (StringUtils.isNotEmpty(payee)) {
                // Update the existing record
                UserPayee userPayee = new UserPayee();
                userPayee.setPayeeNumber(payee);
                userPayee.setUserGuid(userId);
                return payeeApiService.updatePayee(userId, userPayee);
            } else {
                // Delete the existing record
                return payeeApiService.deletePayee(userId);
            }
        }
    }

    private static final Pattern patternGuid = Pattern.compile(".*/users/(.{8}-.{4}-.{4}-.{4}-.{12})");

    /**
     * Convert Keycloak's Location header to this service's Location header. Only handles Locations containing the
     * "users" path.
     * <p>
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
     * Checks the client GUID from the request against the user's roles. Since the roles match by Client ID and the
     * request uses the GUID, we need to do a lookup against Keycloak to get the Client ID.
     */
    boolean isAuthorizedToViewClient(String token, String clientGuid) {
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parseToReadOnly(token);

        LinkedHashMap<String, String> client = (LinkedHashMap<String, String>) keycloakApiService.getClient(clientGuid).getBody();
        // TODO If the client doesn't exist return a 401 (should this be a 404 which is the actual KC response)
        if (client.get("clientId") != null) {
            return authorizedClients.contains(client.get("clientId").toLowerCase());
        } else {
            return false;
        }

    }
    
	boolean isAuthorizedToEditClient(String token, String clientGuid) {
		AuthorizedClientsParser acp = new AuthorizedClientsParser();
		List<String> authorizedClients = acp.parseToEdit(token);
		LinkedHashMap<String, String> client = (LinkedHashMap<String, String>) keycloakApiService.getClient(clientGuid).getBody();
		if (client.get("clientId") != null) {
			return authorizedClients.contains(client.get("clientId").toLowerCase());
		} else {
			return false;
		}

	}


    private MultiValueMap<String, String> buildQueryEventActiveParam(int start, int nbElementMax, Optional<String> dateFrom, Optional<String> dateTo) {

        MultiValueMap<String, String> queryEventParams = new LinkedMultiValueMap<>();
        queryEventParams.add("type", "LOGIN");
        queryEventParams.add("first", String.valueOf(start));
        queryEventParams.add("max", String.valueOf(nbElementMax));
        dateFrom.ifPresent(dateFromValue -> queryEventParams.add("dateFrom", dateFromValue));
        dateTo.ifPresent(dateToValue -> queryEventParams.add("dateTo", dateToValue));
        return queryEventParams;
    }

    private MultiValueMap<String, String> buildQueryEventsByUser(String userId, int start, int nbElementMax, Optional<String> dateFrom, Optional<String> dateTo) {

        MultiValueMap<String, String> queryEventParams = new LinkedMultiValueMap<>();
        queryEventParams.add("type", "LOGIN");
        queryEventParams.add("user", String.valueOf(userId));
        queryEventParams.add("first", String.valueOf(start));
        queryEventParams.add("max", String.valueOf(nbElementMax));
        dateFrom.ifPresent(dateFromValue -> queryEventParams.add("dateFrom", dateFromValue));
        dateTo.ifPresent(dateToValue -> queryEventParams.add("dateTo", dateToValue));
        return queryEventParams;
    }

    @ExceptionHandler(BulkRemovalRequestException.class)
    public ResponseEntity<String> handleBulkRemovalExceptions(BulkRemovalRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
