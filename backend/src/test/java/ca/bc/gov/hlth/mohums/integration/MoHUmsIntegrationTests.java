package ca.bc.gov.hlth.mohums.integration;

import ca.bc.gov.hlth.mohums.userSearch.user.UserDTO;
import net.minidev.json.parser.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class MoHUmsIntegrationTests {

    @Value("${client-test-id}")
    String mohRealmClientId;

    @Value("${client-test-secret}")
    String mohRealmClientSecret;

    @Value("${spring.security.oauth2.client.registration.keycloak-master.client-id}")
    String masterRealmClientId;

    @Value("${spring.security.oauth2.client.registration.keycloak-master.client-secret}")
    String masterRealmClientSecret;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;

    private String jwt;

    @BeforeAll
    public void getJWT() throws InterruptedException, ParseException, IOException {
        jwt = integrationTestsUtils.getMohApplicationsRealmKcToken(mohRealmClientId, mohRealmClientSecret);

        webTestClient = webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(120))
                .build();

    }


    @Test
    public void groupsAuthorizedWithDescriptionsCheck() throws Exception {
        List<Object> groups = webTestClient
                .get()
                .uri("/groups")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk() //HTTP 200
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(groups).isNotEmpty();

        groups.stream().map(g -> (LinkedHashMap) g)
                .forEach(g -> Assertions.assertThat(g.get("description")).isNotNull());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void clientsAuthorized() throws Exception {
        List<Object> clients = getAll("clients");

        Assertions.assertThat(clients).isNotEmpty();
        Assertions.assertThat(clients.stream()
                .map(client -> (LinkedHashMap<String, Object>) client)
                .noneMatch(client -> client.containsKey("secret"))).isTrue();
    }

    @Test
    public void getClientRoles() throws Exception {
        Map<String, ?> client = (Map<String, ?>) getAll("clients").get(0);
        List<Object> clientRoles = webTestClient
                .get()
                .uri("/clients/" + client.get("id") + "/roles")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(clientRoles).isNotEmpty()
                .allSatisfy(this::verifyClientRole);
    }

    private void verifyClientRole(Object clientRole) {
        Assertions.assertThat(clientRole)
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("clientRole", Boolean.TRUE);
    }

    @ParameterizedTest()
    @MethodSource("provideDataForValidateUserSearchQueryParameters")
    public void validateUserSearchQueryParameters(String paramName, Optional<String> paramValue, String expectedResponse) {
               String response = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam(paramName, paramValue)
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

               assertEquals(expectedResponse, response);
    }

    private static Stream<Arguments> provideDataForValidateUserSearchQueryParameters() {
        return Stream.of(
                Arguments.of("org", Optional.of("123org"), "Invalid query parameter. Organization id must contain only numbers."),
                Arguments.of("lastLogAfter", Optional.of("2000-01-01"), "Invalid query parameter. \"Last logged-in after date\" must be in yyyy-mm-dd format."),
                Arguments.of("lastLogBefore", Optional.of("2000-01-01"), "Invalid query parameter. \"Last logged-in before date\" must be in yyyy-mm-dd format."),
                Arguments.of("lastLogBefore", Optional.of("random text"), "Invalid query parameter. \"Last logged-in before date\" must be in yyyy-mm-dd format.")
        );
    }

    @Test
    public void getUsersInRole() throws Exception {

        String clientId = "24447cb4-f3b1-455b-89d9-26c081025fb9"; //UMS-INTEGRATION-TESTS
        String selectedRole = "getUsersInRole_TEST_ROLE";


        final List<UserDTO> usersInRole = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("clientId", clientId)
                                .queryParam("selectedRoles", selectedRole)
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(usersInRole.stream().filter(user -> user.getUsername().equals("umstest"))).isNotEmpty();
    }

    private void verifyUser(Object user) {
        Assertions.assertThat(user)
                .hasNoNullFieldsOrProperties()
                .extracting("username").asString().isNotEmpty();
    }

    @Test
    public void usersAuthorized() throws Exception {
        List<Object> allUsers = getAll("users");

        Assertions.assertThat(allUsers).isNotEmpty()
                .allSatisfy(this::verifyUser);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForSearchBySearchParam")
    public void searchBySearchParam(String searchParam, boolean shouldReturnResults) {

        final List<UserDTO> usersThatSatisfySearchCondition = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("search", searchParam)
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        boolean nonEmptyResultSet = usersThatSatisfySearchCondition.size() > 0;
        assertTrue(nonEmptyResultSet == shouldReturnResults && usersThatSatisfySearchCondition.stream().allMatch(user -> userContainsSearchParam(user, searchParam)));


    }

    private boolean userContainsSearchParam(UserDTO user, String searchParam){
        String username = user.getUsername();
        String email = user.getEmail();
        String lastName = user.getLastName();
        String firstName = user.getFirstName();
        int substringFoundInUser = 0;

        for (String stringToSearch : searchParam.trim().split("\\s+")) {
            if (stringToSearch.length() >= 2 && stringToSearch.charAt(0) == '"' && stringToSearch.charAt(stringToSearch.length() - 1) == '"') {
                //exact search
                String value = stringToSearch.toLowerCase().substring(1, stringToSearch.length() - 1);
                Predicate<String> equalsValue = s -> s!= null && s.toLowerCase().equals(value);
                if(equalsValue.test(username) || equalsValue.test(email) || equalsValue.test(lastName) || equalsValue.test(firstName)){
                    substringFoundInUser ++;
                }
            } else {
                String value = stringToSearch.toLowerCase().replace("*", "");
                Predicate<String> containsValue = s -> s!= null && s.toLowerCase().contains(value);
                if(containsValue.test(username) || containsValue.test(email) || containsValue.test(lastName) || containsValue.test(firstName)){
                    substringFoundInUser ++;
                }
            }
        }

        return substringFoundInUser > 0;
    }

    private static Stream<Arguments> provideValuesForSearchBySearchParam() {
        return Stream.of(Arguments.of("umstest", true),
                Arguments.of("UMStEST", true),
                Arguments.of("UMSTEST", true),
                Arguments.of("test ums", true),
                Arguments.of("test", true),
                Arguments.of("ums", true),
                Arguments.of("Organization UMS", true),
                Arguments.of("@ums.com", false),
                Arguments.of("*@ums.com", true),
                Arguments.of("\"Organization UMS\"", false),
                Arguments.of("\"Organization\"", true)
                );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void searchByOrganization() throws Exception {
        final List<Object> allUsers = getAll("users");
        final List<String> allUsersIds = allUsers.stream().map(user -> (LinkedHashMap<String, Object>) user).map(user -> user.get("id").toString()).collect(Collectors.toList());

        final List<UserDTO> usersWithOrg = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("org", "00000010")
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserDTO.class)
                .returnResult()
                .getResponseBody();

        assertTrue(usersWithOrg.stream().allMatch(user -> allUsersIds.contains(user.getId())));
    }

    @Test
    public void searchByEmailAndOrganization() throws Exception {
        // Given a test user with Org. ID present...
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"enabled\":true,\"username\":\"testWithOrgId\",\"firstName\":\"Test\",\"lastName\":\"WithOrgId\",\"email\":\"test@domain.com\",\"emailVerified\":\"\",\"attributes\":{\"org_details\":[\"{\\\"id\\\":\\\"00001763\\\"}\"]}}")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().value(status -> Assertions.assertThat(status).isIn(
                        // CREATED is returned when the user does not already exist.
                        HttpStatus.CREATED.value(),
                        // CONFLICT is returned when the user already exists.
                        HttpStatus.CONFLICT.value()));

        // ... and another test user with the same e-mail but no Org. ID, ...
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"enabled\":true,\"username\":\"testWithoutOrgId\",\"firstName\":\"Test\",\"lastName\":\"WithoutOrgId\",\"email\":\"test@domain.com\",\"emailVerified\":\"\",\"attributes\":{}}")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().value(status -> Assertions.assertThat(status).isIn(
                        // CREATED is returned when the user does not already exist.
                        HttpStatus.CREATED.value(),
                        // CONFLICT is returned when the user already exists.
                        HttpStatus.CONFLICT.value()));

        // ... when a search is made on that e-mail address AND filtering by Org. ID, ...
        final List<Object> filteredUsers = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("email", "test@domain.com")
                        .queryParam("org", "00001763")
                        .build())
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        // ... then the results should have at least one filtered user
        Assertions.assertThat(filteredUsers).isNotEmpty()
                // with username = "testWithoutOrgId"
                .anySatisfy(filteredUser -> Assertions.assertThat(filteredUser)
                        .extracting("username").asString().isEqualToIgnoringCase("testWithOrgId"));
    }

    @Test
    public void searchByNonExistingOrganization() throws Exception {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("org", "12345678987654321")
                        .build())
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(0);
    }

    @Test
    public void searchUsersByLastLogBefore() throws Exception {
        final List<Object> allUsers = getAll("users");

        final List<Object> filteredUsers = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("first", 0)
                                .queryParam("max", 2000)
                                .queryParam("lastLogBefore",
                                        LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(allUsers).isNotEmpty();
        Assertions.assertThat(filteredUsers).isNotEmpty();
        Assertions.assertThat(allUsers.size()).isGreaterThan(filteredUsers.size());
    }

    @Test
    public void searchUsersByNameAndLastLogAfter() throws Exception {
        final List<Object> allUsers = getAll("users");

        final List<Object> filteredUsers = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("first", 0)
                                .queryParam("max", 2000)
                                .queryParam("firstName", "Filip")
                                .queryParam("lastLogAfter",
                                        LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_DATE))
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(allUsers).isNotEmpty();
        Assertions.assertThat(filteredUsers).isNotEmpty();
        Assertions.assertThat(allUsers.size()).isGreaterThan(filteredUsers.size());
    }

    @Test
    public void searchByUserNameForServiceAccountUsers() {

        final List<Object> usersResponse = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("username", "service-account-")
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(usersResponse).isEmpty();
    }

    @Test
    public void lookupServiceAccountUser() {
        String UMS_SERVICE_ACCOUNT_ID = "7c5ddf30-1754-490b-ae43-ba71cd544e6b";
        webTestClient
                .get()
                .uri(String.format("/users/%s", UMS_SERVICE_ACCOUNT_ID))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void lookupUserAuthorized() throws Exception {
        webTestClient
                .get()
                .uri("/users/abcd-efgh-1234-5678")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void addUser() throws Exception {
        WebTestClient.ResponseSpec created = webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"enabled\":true,\"attributes\":{},\"username\":\"bingo\",\"emailVerified\":\"\"}")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT); //HTTP 409 (Conflict); user already exists
    }

    @Test
    public void updateUser() throws Exception {
        webTestClient
                .put()
                // umstest user
                .uri("/users/c35d48ea-3df9-4758-a27b-94e4cab1ba44")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"attributes\": { \"test_att\": [\"abcd12\"]}}")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //HTTP 204 indicates success
    }

    @Test
    public void addUserClientRole() {
        webTestClient
                .post()
                // umstest user
                // UMS-INTEGRATION-TESTS client
                .uri("/users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/role-mappings/clients/24447cb4-f3b1-455b-89d9-26c081025fb9")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\n"
                        + "    {\n"
                        + "        \"id\": \"e5625153-1cd0-48f7-b305-78339520740a\",\n"
                        + "        \"name\": \"TEST_ROLE\",\n"
                        + "        \"composite\": false,\n"
                        + "        \"clientRole\": true,\n"
                        + "        \"containerId\": \"24447cb4-f3b1-455b-89d9-26c081025fb9\"\n"
                        + "    }\n"
                        + "]")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //HTTP 204 indicates success
    }

    @Test
    public void deleteUserClientRole() {
        webTestClient
                .method(HttpMethod.DELETE)
                // umstest user
                // UMS-INTEGRATION-TESTS client
                .uri("/users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/role-mappings/clients/24447cb4-f3b1-455b-89d9-26c081025fb9")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\n"
                        + "    {\n"
                        + "        \"id\": \"e5625153-1cd0-48f7-b305-78339520740a\",\n"
                        + "        \"name\": \"TEST_ROLE\",\n"
                        + "        \"composite\": false,\n"
                        + "        \"clientRole\": true,\n"
                        + "        \"containerId\": \"24447cb4-f3b1-455b-89d9-26c081025fb9\"\n"
                        + "    }\n"
                        + "]")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //HTTP 204 indicates success
    }

    @Test
    public void getUserGroups() throws Exception {
        webTestClient
                .get()
                // umstest user
                .uri("users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/groups")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void addUserGroups() throws Exception {
        webTestClient
                .put()
                // umstest user
                // CGI QA group
                .uri("users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/groups/1798203d-027f-4856-a445-8a90c1dc9756")
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("{\"groupName\":\"CGI QA group\"}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //HTTP 204 indicates success
    }

    @Test
    public void removeUserGroups() throws Exception {
        webTestClient
                .method(HttpMethod.DELETE)
                // umstest user
                // CGI QA group
                .uri("users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/groups/1798203d-027f-4856-a445-8a90c1dc9756")
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("{\"groupName\":\"CGI QA group\"}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //HTTP 204 indicates success
    }

    @Test
    public void getEvents_smokeTest() throws Exception {
        webTestClient
                .get()
                .uri("events")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void getEvents_hasResults() throws Exception {
        List<Object> responseBody = webTestClient
                .get()
                .uri("events")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult().getResponseBody();

        Assertions.assertThat(responseBody).isNotEmpty();
        Assertions.assertThat(responseBody).first()
                .extracting("realmId").asString()
                .isEqualTo("moh_applications");
    }

    @Test
    public void getEvents_queryLogin_hasOnlyLoginEvents() throws Exception {
        List<Object> responseBody = webTestClient
                .get()
                .uri("events?type=LOGIN")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult().getResponseBody();
        Assertions.assertThat(responseBody).isNotEmpty();
        Assertions.assertThat(responseBody)
                .extracting("type")
                .containsOnly("LOGIN");

    }

    @Test
    public void getAdminEvents_smokeTest() throws Exception {
        webTestClient
                .get()
                .uri("admin-events")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void getAdminEvents_hasResults() throws Exception {
        List<Object> responseBody = webTestClient
                .get()
                .uri("admin-events")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult().getResponseBody();

        Assertions.assertThat(responseBody).isNotEmpty();
        Assertions.assertThat(responseBody).first()
                .extracting("realmId").asString()
                .isEqualTo("moh_applications");
    }

    @Test
    public void getAdminEvents_queryLogin_hasOnlyLoginEvents() throws Exception {
        List<Object> responseBody = webTestClient
                .get()
                .uri("admin-events?resourceTypes=USER&resourceTypes=CLIENT_ROLE_MAPPING")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult().getResponseBody();
        Assertions.assertThat(responseBody).isNotEmpty();
        Assertions.assertThat(responseBody)
                .extracting("resourceType")
                .containsOnly("USER", "CLIENT_ROLE_MAPPING");

    }

    @Test
    public void assignedUserClientRoleMappingUnauthorized() throws Exception {
        webTestClient
                .get()
                // umstest user
                // 1b2ce61a-1235-4a0e-8334-1ac557151757 is the realm-management client, which is not in the list of USER-MANAGEMENT-SERVICE roles.
                .uri("users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/role-mappings/clients/1b2ce61a-1235-4a0e-8334-1ac557151757")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void assignedUserClientRoleMappingAuthorized() throws Exception {
        webTestClient
                .get()
                // umstest user
                // 24447cb4-f3b1-455b-89d9-26c081025fb9 is UMS-INTEGRATION-TESTS, which is in the list of USER-MANAGEMENT-SERVICE roles, i.e. "view-client-ums-integration-tests"
                .uri("users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/role-mappings/clients/24447cb4-f3b1-455b-89d9-26c081025fb9")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void openApiNoAuthRequired() throws Exception {
        webTestClient
                .get()
                .uri("/docs/api-docs")
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void assignedUserClientRoleMappingNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/users/abcd-efgh-1234-5678/role-mappings/clients/1234-efgh-4567-lmno")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void availableUserClientRoleMappingNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/users/abcd-efgh-1234-5678/role-mappings/clients/1234-efgh-4567-lmno/available")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void effectiveUserClientRoleMappingNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/users/abcd-efgh-1234-5678/role-mappings/clients/1234-efgh-4567-lmno/composite")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void groupsNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/groups")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void usersNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/users")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void lookupUserNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/users/abcd-efgh-1234-5678")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void clientsNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/clients")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    private List<Object> getAll(String resource) {
        return webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/" + resource)
                                .queryParam("first", 0)
                                .queryParam("max", 5000)
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    public void getDB_connection() throws Exception {
        DriverManager.getConnection(url, username, password);
    }

    @Test
    public void getMetrics_active_user_count_smokeTest() throws Exception {
        webTestClient
                .get()
                .uri("metrics/active-user-count")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void getMetrics_total_number_of_users_smokeTest() throws Exception {
        webTestClient
                .get()
                .uri("metrics/total-number-of-users")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void getMetrics_unique_user_count_by_idp_smokeTest() throws Exception {
        webTestClient
                .get()
                .uri("metrics/unique-user-count-by-idp")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void getMetrics_unique_user_count_by_realm_smokeTest() throws Exception {
        webTestClient
                .get()
                .uri("metrics/unique-user-count-by-realm")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void getUserFromIdirRealm() throws IOException, InterruptedException, ParseException {
        String access_token = integrationTestsUtils.getMasterRealmKcToken(masterRealmClientId, masterRealmClientSecret);

        String baseUrlIdirRealm = "https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/idir/";

        webTestClient
                .mutate()
                .baseUrl(baseUrlIdirRealm)
                .build()
                .get()
                .uri("users/ee1e2f18-0a1a-4aaa-92c5-6ad645f3c839")//umstest
                .header("Authorization", "Bearer " + access_token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Object.class);
    }

    /*
    This test is ignored by default, since adding linked identities is not automated.
    In order to run the test, add two bcsc_* links (bcsc, bcsc_mspdirect ie.) to umstest user.
     */
    @Disabled("Test disabled until re-adding linked identities is automated")
    @Test
    @SuppressWarnings("unchecked")
    public void removeBothBcscLinkedIdentityTypes() throws IOException, ParseException, InterruptedException {
        //umstest user
        LinkedHashMap<String, Object> user = (LinkedHashMap<String, Object>) webTestClient
                .get()
                .uri("/users/c35d48ea-3df9-4758-a27b-94e4cab1ba44")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Object.class)
                .returnResult()
                .getResponseBody();

        ArrayList<LinkedHashMap<String, String>> federatedIdentities = (ArrayList<LinkedHashMap<String, String>>) user.get("federatedIdentities");
        List<LinkedHashMap<String, String>> bcscLikeFederatedIdentities = getBcscIdentities(federatedIdentities);

        //in this test user needs to be associated with two bcsc idps
        Assertions.assertThat(bcscLikeFederatedIdentities.size()).isEqualTo(2);

        //both links point to the same user, so id is the same
        String bcscRealmUserId = bcscLikeFederatedIdentities.get(0).get("userId");

        //check delete response status
        webTestClient
                .method(HttpMethod.DELETE)
                .uri("/users/c35d48ea-3df9-4758-a27b-94e4cab1ba44/federated-identity/bcsc")
                .header("Authorization", "Bearer " + jwt)
                .bodyValue(bcscRealmUserId)
                .exchange()
                .expectStatus().isNoContent();

        //check that no bcsc links exist
        LinkedHashMap<String, Object> userWithoutBcscLinks = (LinkedHashMap<String, Object>) webTestClient
                .get()
                .uri("/users/c35d48ea-3df9-4758-a27b-94e4cab1ba44")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Object.class)
                .returnResult()
                .getResponseBody();
        federatedIdentities = (ArrayList<LinkedHashMap<String, String>>) userWithoutBcscLinks.get("federatedIdentities");
        Assertions.assertThat(getBcscIdentities(federatedIdentities)).isEmpty();

        //check that user does not exist in bcsc realm
        String access_token = integrationTestsUtils.getMasterRealmKcToken(masterRealmClientId, masterRealmClientSecret);
        String baseUrlBcscRealm = "https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/bcsc/";
        webTestClient
                .mutate()
                .baseUrl(baseUrlBcscRealm)
                .build()
                .get()
                .uri(String.format("users/%s",bcscRealmUserId))
                .header("Authorization", "Bearer " + access_token)
                .exchange()
                .expectStatus().isNotFound();
    }

    private List<LinkedHashMap<String, String>> getBcscIdentities(ArrayList<LinkedHashMap<String, String>> federatedIdentities) {
        return federatedIdentities.stream().filter(fi -> fi.get("identityProvider").contains("bcsc")).collect(Collectors.toList());
    }

}
