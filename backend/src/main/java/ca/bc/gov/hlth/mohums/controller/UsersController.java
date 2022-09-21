package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.exceptions.HttpUnauthorizedException;
import ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser;
import ca.bc.gov.hlth.mohums.util.FilterUserByOrgId;
import ca.bc.gov.hlth.mohums.validator.PermissionsValidator;
import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class UsersController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PermissionsValidator permissionsValidator;

    private final WebClientService webClientService;

    private final String vanityHostname;

    public UsersController(WebClientService webClientService, @Value("${config.vanity-hostname}") String vanityHostname) {
        this.webClientService = webClientService;
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

        ResponseEntity<List<Object>> searchResults = webClientService.getUsers(queryParams);

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
            String customDateCriteria = " AND event_time > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000";
            Optional<String> lastLogBeforeCriteria = Optional.empty();
            if (lastLogAfter.isPresent()) {
                Long lastLogAfterEpoch = LocalDate.parse(lastLogAfter.get()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                customDateCriteria = " AND event_time > " + lastLogAfterEpoch;
            } else {
                Long lastLogBeforeEpoch = LocalDate.parse(lastLogBefore.get()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                lastLogBeforeCriteria = Optional.of(" HAVING MAX(event_time) < " + lastLogBeforeEpoch);
            }

            String sql
                    = "SELECT user_id, MAX(event_time) AS last_login"
                    + "  FROM keycloak.event_entity"
                    + " WHERE type='LOGIN'"
                    + "   AND user_id IS NOT NULL" + customDateCriteria
                    + " GROUP BY user_id";
            if(lastLogBeforeCriteria.isPresent()){
                sql += lastLogBeforeCriteria.get();
            }
            List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sql);

            Map<String, Object> usersLastLogin = new HashMap<>();
            for (Map<String, Object> o : queryResult) {
                usersLastLogin.put(o.get("USER_ID").toString(), o.get("LAST_LOGIN"));
            }

            List<Object> filteredUsersByLastLog = new ArrayList<>();
            for (Object user : users) {
                String userId = ((LinkedHashMap) user).get("id").toString();
                if (!userId.isEmpty() && usersLastLogin.containsKey(userId)) {
                    ((LinkedHashMap) user).put("lastLogDate", usersLastLogin.get(userId));
                    filteredUsersByLastLog.add(user);
                }
            }
            searchResults = ResponseEntity.status(searchResults.getStatusCode()).body(filteredUsersByLastLog);
        }

        return searchResults;
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
        String[] roles = null;
        Map<String, String> userRoleMap = new HashMap<>();
        if (selectedRoles.isEmpty()) {
            //If no roles selected, grab all roles for the selected client
            ResponseEntity res = webClientService.getClientRoles(clientId);
            List<Map> allRoles = (List) res.getBody();
            roles = allRoles.stream().map(r -> (String) r.get("name")).toArray(size -> new String[size]);
        } else {
            roles = selectedRoles.get();
        }
        for (String role : roles) {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("max", "-1");
            ResponseEntity res = webClientService.getUsersInRole(clientId, role, queryParams);
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

    @GetMapping("/users/{userId}/last-logins")
    public ResponseEntity<Object> getUserLogins(@PathVariable String userId) {
        int maxRecords = 5000;
        LocalDate oneYearAgo = LocalDate.now().minus(1, ChronoUnit.YEARS);
        Optional<String> oneYearAgoParam = Optional.of(oneYearAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        MultiValueMap<String, String> params = buildQueryEventsByUser(userId, 0, maxRecords, oneYearAgoParam, Optional.empty());
        List<Map<String, Object>> logins = (List<Map<String, Object>>) webClientService.getEvents(params).getBody();

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
        return webClientService.getUserGroups(userId);
    }

    @PutMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Object> addUserGroups(@PathVariable String userId,
                                                @PathVariable String groupId,
                                                @RequestBody String groupName,
                                                @AuthenticationPrincipal Jwt jwt) {
        return permissionsValidator.validateGroupManagementPermission(jwt, groupName) ?
                webClientService.addUserGroups(userId, groupId) : ResponseEntity.status(HttpStatus.FORBIDDEN).body("Add user to group - permission denied");
    }

    @DeleteMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<Object> removeUserGroups(@PathVariable String userId,
                                                   @PathVariable String groupId,
                                                   @RequestBody String groupName,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return permissionsValidator.validateGroupManagementPermission(jwt, groupName) ?
                webClientService.removeUserGroups(userId, groupId) : ResponseEntity.status(HttpStatus.FORBIDDEN).body("Remove user from group - permission denied");
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

        LinkedHashMap<String, String> client = (LinkedHashMap<String, String>) webClientService.getClient(clientGuid).getBody();
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
