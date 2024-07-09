package ca.bc.gov.hlth.mohums.integration;

import ca.bc.gov.hlth.mohums.model.BulkRemovalRequest;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class BulkRemovalIntegrationTests {

    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    @Value("${spring.security.oauth2.client.provider.keycloak-moh.token-uri}")
    String keycloakTokenUri;

    @Value("${client-test-id}")
    String clientId;

    @Value("${client-test-secret}")
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

    private static final String UMS_INTEGRATION_TESTS_CLIENT_ID = "24447cb4-f3b1-455b-89d9-26c081025fb9";
    private static final String BULK_REMOVAL_USER_UMS_1 = "3d78de77-86dc-41a3-a3d9-432a494d9147";
    private static final String BULK_REMOVAL_USER_UMS_2 = "ead9626f-df90-4c95-91d4-e4e447afde5f";
    private static final String NON_EXISTING = "non-existing";

    private LinkedHashMap<String, Object> createRoleRepresentation(String id, String name, String containerId) {
        LinkedHashMap<String, Object> roleRepresentation = new LinkedHashMap<>();
        roleRepresentation.put("id", id);
        roleRepresentation.put("name", name);
        roleRepresentation.put("composite", false);
        roleRepresentation.put("clientRole", true);
        roleRepresentation.put("containerId", containerId);

        return roleRepresentation;
    }

    private LinkedHashMap<String, Object> getBulkRemovalRole1() {
        return createRoleRepresentation("ea6dcb83-f11b-4ff3-a725-c7a70477af8d", "bulk-removal-role-1", UMS_INTEGRATION_TESTS_CLIENT_ID);
    }

    private LinkedHashMap<String, Object> getBulkRemovalRole2() {
        return createRoleRepresentation("ab06dd06-280f-4b6b-8c99-39eb8639d292", "bulk-removal-role-2", UMS_INTEGRATION_TESTS_CLIENT_ID);
    }

    private LinkedHashMap<String, Object> getNotAssignedRole() {
        return createRoleRepresentation("e5625153-1cd0-48f7-b305-78339520740a", "TEST_ROLE", UMS_INTEGRATION_TESTS_CLIENT_ID);
    }

    private LinkedHashMap<String, Object> getNonExistingRole() {
        return createRoleRepresentation(NON_EXISTING, NON_EXISTING, UMS_INTEGRATION_TESTS_CLIENT_ID);
    }

    private LinkedHashMap<String, Object> getRoleFromNonExistingClient() {
        return createRoleRepresentation("ea6dcb83-f11b-4ff3-a725-c7a70477af8d", "bulk-removal-role-1", NON_EXISTING);
    }

    private List<Object> getAssignedUserClientRoleMapping(String userId) {
        return webTestClient.method(HttpMethod.GET)
                .uri(String.format("/users/%s/role-mappings/clients/%s", userId, UMS_INTEGRATION_TESTS_CLIENT_ID))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();
    }

    private void addTestRoles(String userId) {
        String uri = String.format("/users/%s/role-mappings/clients/%s", userId, UMS_INTEGRATION_TESTS_CLIENT_ID);
        webTestClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(getBulkRemovalRole1(), getBulkRemovalRole2()))
                .header("Authorization", "Bearer " + jwt)
                .exchange();
    }

    private List<Object> bulkRemove(BulkRemovalRequest bulkRemovalRequest, String clientId) {
        return webTestClient
                .method(HttpMethod.DELETE)
                .uri("/bulk-removal/" + clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bulkRemovalRequest)
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleOneUserSuccess() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NO_CONTENT", responseItem.get("statusCode"));

        List<Object> remainingRoles = getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1);
        assertEquals(1, remainingRoles.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesOneUserSuccess() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NO_CONTENT", responseItem.get("statusCode"));

        List<Object> remainingRoles = getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1);
        assertEquals(0, remainingRoles.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleOneUserFailureUserDoesNotExist() {
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(NON_EXISTING, List.of(getBulkRemovalRole1())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NOT_FOUND", responseItem.get("statusCode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleOneUserFailureRoleDoesNotExist() {
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getNonExistingRole())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NOT_FOUND", responseItem.get("statusCode"));
    }

    //This is a WIP, part of error-handling work
    @Disabled
    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleOneUserFailureClientDoesNotExist() {
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, NON_EXISTING);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NOT_FOUND", responseItem.get("statusCode"));
    }

    //Keycloak api works this way - pass list of roles to be deleted, if one of them is 404, request fails
    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesOneUserOneRoleDoesNotExist() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1(), getNonExistingRole())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NOT_FOUND", responseItem.get("statusCode"));

        List<Object> remainingRoles = getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1);
        assertEquals(2, remainingRoles.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleOneUserSuccessRoleNotAssigned() {
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getNotAssignedRole())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NO_CONTENT", responseItem.get("statusCode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleTwoUsersSuccess() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);
        addTestRoles(BULK_REMOVAL_USER_UMS_2);

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1()),
                BULK_REMOVAL_USER_UMS_2, List.of(getBulkRemovalRole1())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .allMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));

        assertEquals(1, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1).size());
        assertEquals(1, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_2).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesTwoUsersSuccess() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);
        addTestRoles(BULK_REMOVAL_USER_UMS_2);

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_2, List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .allMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));

        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_2).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleTwoUsersPartialFailureUserDoesNotExist() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1()),
                        NON_EXISTING, List.of(getBulkRemovalRole1())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NOT_FOUND")));

        assertEquals(1, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesTwoUsersOneRoleDoesNotExist() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);
        addTestRoles(BULK_REMOVAL_USER_UMS_2);

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1(), getNonExistingRole()),
                        BULK_REMOVAL_USER_UMS_2, List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NOT_FOUND")));

        assertEquals(2, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_2).size());
    }

}
