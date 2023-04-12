package ca.bc.gov.hlth.mohums.integration;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(Lifecycle.PER_CLASS)
public class MoHUmsIntegrationTests {

    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    @Value("${spring.security.oauth2.client.provider.keycloak-moh.token-uri}")
    String keycloakTokenUri;

    @Value("${spring.security.oauth2.client.registration.keycloak-moh.client-test-id}")
    String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak-moh.client-test-secret}")
    String clientSecret;

    @Value("${spring.security.oauth2.client.registration.keycloak-master.client-id}")
    String masterRealmClientId;

    @Value("${spring.security.oauth2.client.registration.keycloak-master.client-secret}")
    String masterRealmClientSecret;

    @Value("${keycloak-moh.organizations-api-url}")
    private String organizationsApiBaseUrl;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private WebTestClient webTestClient;

    private String jwt;

    @BeforeAll
    public void getJWT() throws InterruptedException, ParseException, IOException {
        jwt = getKcAccessToken();

        webTestClient = webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(120))
                .build();

    }

    private String getKcAccessToken() throws IOException, InterruptedException, ParseException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakTokenUri))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("cache-control", "no-cache")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBodyAsJson = (JSONObject) jsonParser.parse(response.body());
        String access_token = responseBodyAsJson.get("access_token").toString();

        return access_token;
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
    public void clientsAuthorized() throws Exception {
        List<Object> clients = getAll("clients");

        Assertions.assertThat(clients).isNotEmpty();
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

    @Test
    public void getUsersInRole() throws Exception {
        Map<String, ?> client = (Map<String, ?>) getAll("clients").get(0);
        Map<String, ?> clientRole = (Map<String, ?>) webTestClient
                .get()
                .uri("/clients/" + client.get("id") + "/roles")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody().get(0);

        List<Object> usersInRole = webTestClient
                .get()
                .uri("/clients/"
                        + client.get("id")
                        + "/roles/"
                        + clientRole.get("name")
                        + "/users")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(usersInRole).isNotEmpty()
                .allSatisfy(this::verifyUser);
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

    @Test
    public void searchByOrganization() throws Exception {
        final List<Object> allUsers = getAll("users");

        final List<Object> filteredUsers = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("org", "00000010")
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

        Assertions.assertThat(filteredUsers).isNotEmpty();
        Assertions.assertThat(allUsers).containsAll(filteredUsers);
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
                        .queryParam("org", "non_existing_org_id")
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
                // 123-tschiavo user
                .uri("/users/5faec8ce-f40c-4bf4-9862-9778e1533dd4")
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
                // 123-tschiavo user
                // PLR client
                .uri("/users/5faec8ce-f40c-4bf4-9862-9778e1533dd4/role-mappings/clients/dc7b9502-3ffa-4ff8-be2e-ebfebe650590")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\n"
                        + "    {\n"
                        + "        \"id\": \"30f494e9-41c7-40eb-9660-a14bb8248af4\",\n"
                        + "        \"name\": \"REG_ADMIN\",\n"
                        + "        \"composite\": false,\n"
                        + "        \"clientRole\": true,\n"
                        + "        \"containerId\": \"dc7b9502-3ffa-4ff8-be2e-ebfebe650590\"\n"
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
                // 123-tschiavo user
                // PLR client
                .uri("/users/5faec8ce-f40c-4bf4-9862-9778e1533dd4/role-mappings/clients/dc7b9502-3ffa-4ff8-be2e-ebfebe650590")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\n"
                        + "    {\n"
                        + "        \"id\": \"30f494e9-41c7-40eb-9660-a14bb8248af4\",\n"
                        + "        \"name\": \"REG_ADMIN\",\n"
                        + "        \"composite\": false,\n"
                        + "        \"clientRole\": true,\n"
                        + "        \"containerId\": \"dc7b9502-3ffa-4ff8-be2e-ebfebe650590\"\n"
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
                // 123-tschiavo user
                .uri("users/5faec8ce-f40c-4bf4-9862-9778e1533dd4/groups")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void addUserGroups() throws Exception {
        webTestClient
                .put()
                // 123-tschiavo user
                // CGI QA group
                .uri("users/5faec8ce-f40c-4bf4-9862-9778e1533dd4/groups/1798203d-027f-4856-a445-8a90c1dc9756")
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("{\"groupName\":\"CGI QA group\"}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //HTTP 204 indicates success
    }

    @Test
    public void removeUserGroups() throws Exception {
        webTestClient
                .method(HttpMethod.DELETE)
                // 123-tschiavo user
                // CGI QA group
                .uri("users/5faec8ce-f40c-4bf4-9862-9778e1533dd4/groups/1798203d-027f-4856-a445-8a90c1dc9756")
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
                // 123-tschiavo user
                // 1b2ce61a-1235-4a0e-8334-1ac557151757 is the realm-management client, which is not in the list of USER-MANAGEMENT-SERVICE roles.
                .uri("users/5faec8ce-f40c-4bf4-9862-9778e1533dd4/role-mappings/clients/1b2ce61a-1235-4a0e-8334-1ac557151757")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void assignedUserClientRoleMappingAuthorized() throws Exception {
        webTestClient
                .get()
                // 123-tschiavo user
                // a425bf07-a2bd-403f-a605-afc2b4898c3f is PLR, which is in the list of USER-MANAGEMENT-SERVICE roles, i.e. "view-client-plr"
                .uri("users/5faec8ce-f40c-4bf4-9862-9778e1533dd4/role-mappings/clients/dc7b9502-3ffa-4ff8-be2e-ebfebe650590")
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

        String keycloakTokenUri = "https://common-logon-dev.hlth.gov.bc.ca/auth/realms/master/protocol/openid-connect/token";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakTokenUri))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("cache-control", "no-cache")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=" + masterRealmClientId + "&client_secret=" + masterRealmClientSecret))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBodyAsJson = (JSONObject) jsonParser.parse(response.body());
        String access_token = responseBodyAsJson.get("access_token").toString();


        String baseUrlIdirRealm = "https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/idir/";

        webTestClient
                .mutate()
                .baseUrl(baseUrlIdirRealm)
                .build()
                .get()
                .uri("users/0fe98ff5-3a8b-4336-9d77-d8377e11ba3d")//fflorek
                .header("Authorization", "Bearer " + access_token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Object.class);

    }
}
