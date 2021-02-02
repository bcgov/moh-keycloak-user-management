package ca.bc.gov.hlth.mohums.integration;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
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
import java.util.List;
import org.assertj.core.api.Assertions;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(Lifecycle.PER_CLASS)
public class MoHUmsIntegrationTests {

    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    String keycloakTokenUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    String clientSecret;

    @Autowired
    private WebTestClient webTestClient;

    private String jwt;

    @BeforeAll
    public void getJWT() throws InterruptedException, ParseException, IOException {
        jwt = getKcAccessToken();
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
    public void groupsAuthorized() throws Exception {
        webTestClient
                .get()
                .uri("/groups")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void clientsAuthorized() throws Exception {
        webTestClient
                .get()
                .uri("/clients")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void usersAuthorized() throws Exception {
        final List<Object> allUsers = getAllUsers();
        
        Assertions.assertThat(allUsers).isNotEmpty();
    }

    private List<Object> getAllUsers() {
        return webTestClient
                .get()
                .uri("/users")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    public void searchByOrganization() throws Exception {
        final List<Object> allUsers = getAllUsers();

        final List<Object> filteredUsers = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("org", "00001763")
                        .build())
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
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
        // We expect a 409 (Conflict) because the user already exists.
    }

    @Test
    public void updateUser() throws Exception {
        webTestClient
                .put()
                .uri("/users/39f73cbd-dbf0-41c6-a45c-997c44c1c952")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"attributes\": { \"test_att\": [\"abcd12\"]}}")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void addUserClientRole() {
        //FMDB Client
        //123-tschiavo user
        webTestClient
                .post()
                .uri("/users/39f73cbd-dbf0-41c6-a45c-997c44c1c952/role-mappings/clients/db9dd8ab-0f38-4471-b396-e2ddac45a001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\n" +
                        "    {\n" +
                        "        \"id\": \"a88f491a-3bd1-46ce-9cf6-c509f9a916f8\",\n" +
                        "        \"name\": \"PSDADMIN\",\n" +
                        "        \"composite\": false,\n" +
                        "        \"clientRole\": true,\n" +
                        "        \"containerId\": \"db9dd8ab-0f38-4471-b396-e2ddac45a001\"\n" +
                        "    }\n" +
                        "]")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //204 indicates success
    }

    @Test
    public void deleteUserClientRole() {
        //FMDB Client
        //123-tschiavo user
        webTestClient
                .method(HttpMethod.DELETE)
                .uri("/users/39f73cbd-dbf0-41c6-a45c-997c44c1c952/role-mappings/clients/db9dd8ab-0f38-4471-b396-e2ddac45a001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\n" +
                        "    {\n" +
                        "        \"id\": \"a88f491a-3bd1-46ce-9cf6-c509f9a916f8\",\n" +
                        "        \"name\": \"PSDADMIN\",\n" +
                        "        \"composite\": false,\n" +
                        "        \"clientRole\": true,\n" +
                        "        \"containerId\": \"db9dd8ab-0f38-4471-b396-e2ddac45a001\"\n" +
                        "    }\n" +
                        "]")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //204 indicates success
    }

    @Test
    public void getUserGroups() throws Exception {
        webTestClient
                .get()
                // 123-tschiavo user
                .uri("users/39f73cbd-dbf0-41c6-a45c-997c44c1c952/groups")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void addUserGroups() throws Exception {
        webTestClient
                .put()
                // 123-tschiavo user
                .uri("users/39f73cbd-dbf0-41c6-a45c-997c44c1c952/groups/1798203d-027f-4856-a445-8a90c1dc9756")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //204 indicates success
    }

    @Test
    public void removeUserGroups() throws Exception {
        webTestClient
                .delete()
                // 123-tschiavo user
                .uri("users/39f73cbd-dbf0-41c6-a45c-997c44c1c952/groups/1798203d-027f-4856-a445-8a90c1dc9756")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NO_CONTENT); //204 indicates success
    }

    @Test
    public void assignedUserClientRoleMappingUnauthorized() throws Exception {
        webTestClient
                .get()
                // 1b2ce61a-1235-4a0e-8334-1ac557151757 is the realm-management client, which is not in the list of USER-MANAGEMENT-SERVICE roles.
                .uri("users/39f73cbd-dbf0-41c6-a45c-997c44c1c952/role-mappings/clients/1b2ce61a-1235-4a0e-8334-1ac557151757")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void assignedUserClientRoleMappingAuthorized() throws Exception {
        webTestClient
                .get()
                // a425bf07-a2bd-403f-a605-afc2b4898c3f is GIS, which is in the list of USER-MANAGEMENT-SERVICE roles, i.e. "view-client-gis"
                .uri("users/39f73cbd-dbf0-41c6-a45c-997c44c1c952/role-mappings/clients/a425bf07-a2bd-403f-a605-afc2b4898c3f")
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

}
