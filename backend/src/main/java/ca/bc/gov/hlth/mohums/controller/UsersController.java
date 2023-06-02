package ca.bc.gov.hlth.mohums.controller;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.hlth.mohums.exceptions.HttpUnauthorizedException;
import ca.bc.gov.hlth.mohums.model.UserPayee;
import ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser;
import ca.bc.gov.hlth.mohums.util.FilterUserByOrgId;
import ca.bc.gov.hlth.mohums.validator.PermissionsValidator;
import ca.bc.gov.hlth.mohums.webclient.KeycloakApiService;
import ca.bc.gov.hlth.mohums.webclient.PayeeApiService;

@RestController
public class UsersController {
	
    private static final String KEY_PAYEE_NUMBER = "payeeNumber";

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private PermissionsValidator permissionsValidator;

    private final KeycloakApiService keycloakApiService;
    
    private final PayeeApiService payeeApiService;

    private final String vanityHostname;

    public UsersController(KeycloakApiService keycloakApiService, PayeeApiService payeeApiService, @Value("${config.vanity-hostname}") String vanityHostname) {
        this.keycloakApiService = keycloakApiService;
        this.payeeApiService = payeeApiService;
        this.vanityHostname = vanityHostname;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Object>> getUsers(
            @RequestParam Optional<Boolean> briefRepresentation,
            @RequestParam Optional<String> email,
            @RequestParam Optional<Integer> first,
            @RequestParam Optional<String> firstName,
            @RequestParam Optional<String> lastName,
            @RequestParam Optional<Integer> max,
            @RequestParam Optional<String> search,
            @RequestParam Optional<String> username,
            @RequestParam Optional<String> org,
            @RequestParam Optional<String> lastLogAfter,
            @RequestParam Optional<String> lastLogBefore,
            @RequestParam Optional<String> clientName,
            @RequestParam Optional<String> clientId,
            @RequestParam Optional<String[]> selectedRoles) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        briefRepresentation.ifPresent(briefRepresentationValue -> queryParams.add("briefRepresentation",
                briefRepresentationValue.toString()));
        email.ifPresent(emailValue -> queryParams.add("email", emailValue));
        first.ifPresent(firstValue -> queryParams.add("first", firstValue.toString()));
        firstName.ifPresent(firstNameValue -> queryParams.add("firstName", firstNameValue));
        lastName.ifPresent(lastNameValue -> queryParams.add("lastName", lastNameValue));
        max.ifPresent(maxValue -> queryParams.add("max", maxValue.toString()));
        search.ifPresent(searchValue -> queryParams.add("search", searchValue));
        username.ifPresent(usernameValue -> queryParams.add("username", usernameValue));

        ResponseEntity<List<Object>> searchResults = keycloakApiService.getUsers(queryParams);

        List<Object> users = searchResults.getBody();

        if (org.isPresent() && !CollectionUtils.isEmpty(users)) {
            List<Object> filteredUsers = users.stream().filter(new FilterUserByOrgId(org.get())).collect(Collectors.toList());
            users = filteredUsers;

            searchResults = ResponseEntity.status(searchResults.getStatusCode()).body(filteredUsers);
        }

        //Filter based on selected client & roles
        if (clientId.isPresent()) {
            users = filterUsersByRole(selectedRoles, clientId.get(), users);
            searchResults = ResponseEntity.status(searchResults.getStatusCode()).body(users);
        }

        if ((lastLogAfter.isPresent() || lastLogBefore.isPresent()) && !CollectionUtils.isEmpty(users)) {
            Map<String, Object> namedParameters = new HashMap<>();
            String customDateCriteria = " AND ee.event_time > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000";
            Optional<String> lastLogBeforeCriteria = Optional.empty();
            if (lastLogAfter.isPresent()) {
                Long lastLogAfterEpoch = LocalDate.parse(lastLogAfter.get()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                customDateCriteria = " AND ee.event_time > :lastLogAfterEpoch";
                namedParameters.put("lastLogAfterEpoch", lastLogAfterEpoch);
            } else {
                Long lastLogBeforeEpoch = LocalDate.parse(lastLogBefore.get()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                lastLogBeforeCriteria = Optional.of(" HAVING MAX(ee.event_time) < :lastLogBeforeEpoch");
                namedParameters.put("lastLogBeforeEpoch", lastLogBeforeEpoch);
            }
            Optional<String> clientAndRolesJoins = Optional.empty();
            Optional<String> clientAndRolesCriteria = Optional.empty();
            if (clientName.isPresent() && clientId.isPresent() && clientNameAndIdAreValid(clientName.get(), clientId.get())) {
                clientAndRolesJoins = Optional.of(
                        " JOIN keycloak.user_role_mapping urm ON urm.user_id = ue.id"
                                + " JOIN keycloak.keycloak_role kr ON kr.id = urm.role_id"
                                + " JOIN keycloak.client c ON c.id = kr.client"
                );
                clientAndRolesCriteria = Optional.of(
                        " AND ((ee.client_id IS NOT NULL AND ee.client_id = :clientName) OR (ee.client_id IS NULL AND c.client_id = :clientName))"
                                + " AND kr.name IN (:selectedRoles)"
                );
                namedParameters.put("clientName", clientName.get());
                namedParameters.put("selectedRoles", Arrays.asList(getSelectedRolesForChosenClient(selectedRoles, clientId.get())));
            }
            final String DO_NOT_INCLUDE = "";

            String sql
                    = "SELECT ue.id, MAX(ee.event_time) AS LAST_LOGIN"
                    + "  FROM keycloak.user_entity ue"
                    + "  JOIN keycloak.event_entity ee ON ee.user_id = ue.id"
                    + clientAndRolesJoins.orElse(DO_NOT_INCLUDE)
                    + " WHERE ee.type='LOGIN'" + customDateCriteria
                    + clientAndRolesCriteria.orElse(DO_NOT_INCLUDE)
                    + " GROUP BY ue.id"
                    + lastLogBeforeCriteria.orElse(DO_NOT_INCLUDE);

            List<Map<String, Object>> queryResult = namedParameterJdbcTemplate.queryForList(sql, namedParameters);

            Map<String, Object> usersLastLogin = new HashMap<>();
            for (Map<String, Object> o : queryResult) {
                usersLastLogin.put(o.get("id").toString(), o.get("LAST_LOGIN"));
            }

            List<Object> filteredUsersByLastLog = new ArrayList<>();
            for (Object user : users) {
                String userId = ((LinkedHashMap) user).get("id").toString();
                if (!userId.isEmpty() && usersLastLogin.containsKey(userId)) {
                    ((LinkedHashMap) user).put("lastLogDate", usersLastLogin.get(userId));
                    filteredUsersByLastLog.add(user);
                }
            }

            //Users who haven't logged in for over a year
            if (lastLogBefore.isPresent()) {
                //Users who have those roles but no events connected to them
                sql
                    = "SELECT ue.id"
                    + "  FROM keycloak.user_entity ue"
                    + "  LEFT JOIN keycloak.event_entity ee ON ee.user_id = ue.id"
                    + clientAndRolesJoins.orElse(DO_NOT_INCLUDE)
                    + " WHERE ee.user_id IS NULL"
                    + clientAndRolesCriteria.orElse(DO_NOT_INCLUDE)
                    + "   AND ue.created_timestamp < ((SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000)";

                queryResult = namedParameterJdbcTemplate.queryForList(sql, namedParameters);

                Map<String, Object> inactiveForOverYearIds = new HashMap<>();
                for (Map<String, Object> o : queryResult) {
                    inactiveForOverYearIds.put(o.get("id").toString(), "Over a year ago");
                }

                List<Object> usersInactiveForOverYear = new ArrayList<>();
                for (Object user : users) {
                    String userId = ((LinkedHashMap) user).get("id").toString();
                    if (!userId.isEmpty() && inactiveForOverYearIds.containsKey(userId)) {
                        ((LinkedHashMap) user).put("lastLogDate", inactiveForOverYearIds.get(userId));
                        usersInactiveForOverYear.add(user);
                    }
                }
                filteredUsersByLastLog.addAll(usersInactiveForOverYear);
            }
            searchResults = ResponseEntity.status(searchResults.getStatusCode()).body(filteredUsersByLastLog);
        }
        return searchResults;
    }

    private boolean clientNameAndIdAreValid(String clientName, String clientId) {
        LinkedHashMap client = (LinkedHashMap) keycloakApiService.getClient(clientId).getBody();
        return client.get("name").equals(clientName);
    }

    private String[] getSelectedRolesForChosenClient(Optional<String[]> selectedRoles, String clientId) {
        if (selectedRoles.isEmpty()) {
            //If no roles selected, grab all roles for the selected client
            ResponseEntity res = keycloakApiService.getClientRoles(clientId);
            List<Map> allRoles = (List) res.getBody();
            return allRoles.stream().map(r -> (String) r.get("name")).toArray(size -> new String[size]);
        } else {
            return selectedRoles.get();
        }
    }

    /**
     * Filter the results by the selected clientId, and optionally the list of selected roles If no roles selected, use
     * all roles for that client.
     *
     * @param selectedRoles - Set of roles passed in as search parameters
     * @param clientId      - cientId passed in as search parameter
     * @param users         - Unfiltered search results
     * @return List
     */
    private List filterUsersByRole(Optional<String[]> selectedRoles, String clientId, List users) {
        List<Object> filteredUsers = new ArrayList<>();
        String[] roles = getSelectedRolesForChosenClient(selectedRoles, clientId);
        Map<String, String> userRoleMap = new HashMap<>();
        for (String role : roles) {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("max", "-1");
            ResponseEntity res = keycloakApiService.getUsersInRole(clientId, role, queryParams);
            List<Map> usersInRole = (List) res.getBody();
            for (Map u : usersInRole) {
                String key = (String) u.get("id");
                if (userRoleMap.containsKey(key)) {
                    userRoleMap.put(key, userRoleMap.get(key) + ", " + role);
                } else {
                    userRoleMap.put(key, role);
                }
            }
        }
        for (Object user : users) {
            Map userMap = (Map) user;
            if (userRoleMap.containsKey((String) userMap.get("id"))) {
                //Store the role in the user object for frontend display
                userMap.put("role", userRoleMap.get((String) userMap.get("id")));
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
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
        if (isAuthorizedToViewClient(token, clientGuid)) {
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
        if (isAuthorizedToViewClient(token, clientGuid)) {
            return keycloakApiService.deleteUserClientRole(userId, clientGuid, body);
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
    public ResponseEntity<Object> removeUserIdentityProviderLinks(@PathVariable String userId, @PathVariable String identityProvider, @RequestBody String userIdIdpRealm){
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
        List<String> authorizedClients = acp.parse(token);

        LinkedHashMap<String, String> client = (LinkedHashMap<String, String>) keycloakApiService.getClient(clientGuid).getBody();
        // TODO If the client doesn't exist return a 401 (should this be a 404 which is the actual KC response)
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
}
