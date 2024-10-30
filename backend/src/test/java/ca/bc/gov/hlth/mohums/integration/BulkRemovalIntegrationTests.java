package ca.bc.gov.hlth.mohums.integration;

import ca.bc.gov.hlth.mohums.model.BulkRemovalRequest;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class BulkRemovalIntegrationTests {

    @Value("${client-test-id}")
    String clientId;

    @Value("${client-test-secret}")
    String clientSecret;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;

    private String jwt;

    private static Map<String, String> testUsers = new HashMap<>();

    @BeforeAll
    public void getJWT() throws InterruptedException, ParseException, IOException {
        jwt = integrationTestsUtils.getMohApplicationsRealmKcToken(clientId, clientSecret);

        webTestClient = webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(120))
                .build();

        createTestUsersIfNotExist();

    }

    @SuppressWarnings("unchecked")
    private void createTestUsersIfNotExist() {

        List<String> usernames = new ArrayList<>();
        for (int i=1; i<=10; i++){
            usernames.add(String.format("bulk-removal-test-user-%s", i));
        }

        usernames.forEach( username -> {
            List<Object> user = getUser(username);
            if(user.isEmpty()){
                user = createUserAndGetDetails(username);
            }
            LinkedHashMap<String, Object> userDetails = (LinkedHashMap<String, Object>) user.get(0);
            testUsers.put((String)userDetails.get("username"), (String) userDetails.get("id"));
        });

    }

    private List<Object> getUser(String username){
        final List<Object> usersResponse = webTestClient
                .get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users")
                                .queryParam("username", username)
                                .build()
                )
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        return usersResponse;
    }

    private List<Object> createUserAndGetDetails(String username){
        String body = String.format("{\"enabled\":true,\"attributes\":{},\"username\":\"%s\"}", username);
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED);
        return getUser(username);
    }

    /**
     * Constants used for integration tests:
     * UMS_INTEGRATION_TESTS_CLIENT_ID -> ID of a Keycloak DEV client (service account) who is calling the bulk-removal endpoint.
     * NON_EXISTING - constant for unhappy path testing. Used to mimic invalid user or role ID.
     * Roles from UMS_INTEGRATION_TESTS client
     */

    private static final String UMS_INTEGRATION_TESTS_CLIENT_ID = "24447cb4-f3b1-455b-89d9-26c081025fb9";
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

    @ParameterizedTest
    @MethodSource("provideInvalidArgumentsForBulkRemove")
    public void bulkRemoveInvalidRequestBody(BulkRemovalRequest request, String message) {
        String response = webTestClient
                .method(HttpMethod.DELETE)
                .uri("/bulk-removal/" + UMS_INTEGRATION_TESTS_CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertEquals(message, response);
    }

    private static Stream<Arguments> provideInvalidArgumentsForBulkRemove() {
        return Stream.of(
                Arguments.of(new BulkRemovalRequest(Collections.emptyMap()), "UserRolesForRemoval cannot be empty"),
                Arguments.of(new BulkRemovalRequest(), "UserRolesForRemoval cannot be null"),
                Arguments.of(new BulkRemovalRequest(Map.of("", List.of("role"))), "User ID cannot be null or empty"),
                Arguments.of(new BulkRemovalRequest(Map.of(testUsers.get("bulk-removal-test-user-1"), Collections.emptyList())), "List of roles to remove cannot be null or empty")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleOneUserSuccess() {
        addTestRoles(testUsers.get("bulk-removal-test-user-1"));

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getBulkRemovalRole1())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NO_CONTENT", responseItem.get("statusCode"));

        List<Object> remainingRoles = getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-1"));
        assertEquals(1, remainingRoles.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesOneUserSuccess() {
        addTestRoles(testUsers.get("bulk-removal-test-user-1"));

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NO_CONTENT", responseItem.get("statusCode"));

        List<Object> remainingRoles = getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-1"));
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
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getNonExistingRole())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NOT_FOUND", responseItem.get("statusCode"));
    }

    //Keycloak api works this way - pass list of roles to be deleted, if one of them is 404, request fails
    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesOneUserOneRoleDoesNotExist() {
        addTestRoles(testUsers.get("bulk-removal-test-user-1"));
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getBulkRemovalRole1(), getNonExistingRole())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NOT_FOUND", responseItem.get("statusCode"));

        List<Object> remainingRoles = getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-1"));
        assertEquals(2, remainingRoles.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleOneUserSuccessRoleNotAssigned() {
        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getNotAssignedRole())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(1, response.size());
        LinkedHashMap<String, Object> responseItem = (LinkedHashMap<String, Object>) response.get(0);
        assertEquals("NO_CONTENT", responseItem.get("statusCode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleTwoUsersSuccess() {
        addTestRoles(testUsers.get("bulk-removal-test-user-1"));
        addTestRoles(testUsers.get("bulk-removal-test-user-2"));

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getBulkRemovalRole1()),
                        testUsers.get("bulk-removal-test-user-2"), List.of(getBulkRemovalRole1())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .allMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));

        assertEquals(1, getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-1")).size());
        assertEquals(1, getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-2")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesTwoUsersSuccess() {
        addTestRoles(testUsers.get("bulk-removal-test-user-1"));
        addTestRoles(testUsers.get("bulk-removal-test-user-2"));

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        testUsers.get("bulk-removal-test-user-2"), List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .allMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));

        assertEquals(0, getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-1")).size());
        assertEquals(0, getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-2")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveOneRoleTwoUsersPartialFailureUserDoesNotExist() {
        addTestRoles(testUsers.get("bulk-removal-test-user-1"));

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getBulkRemovalRole1()),
                        NON_EXISTING, List.of(getBulkRemovalRole1())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NOT_FOUND")));

        assertEquals(1, getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-1")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesTwoUsersOneRoleDoesNotExist() {
        addTestRoles(testUsers.get("bulk-removal-test-user-1"));
        addTestRoles(testUsers.get("bulk-removal-test-user-2"));

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(testUsers.get("bulk-removal-test-user-1"), List.of(getBulkRemovalRole1(), getNonExistingRole()),
                        testUsers.get("bulk-removal-test-user-2"), List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(2, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .anyMatch(responseItem -> responseItem.get("statusCode").equals("NOT_FOUND")));

        assertEquals(2, getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-1")).size());
        assertEquals(0, getAssignedUserClientRoleMapping(testUsers.get("bulk-removal-test-user-2")).size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesTenUsersSuccess() {
        Map<String, List<Object>> bulkRemovalRequestBody = new HashMap<>();
        testUsers.forEach((username, id) -> {
            addTestRoles(id);
            bulkRemovalRequestBody.put(id, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()));
        });

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(bulkRemovalRequestBody);

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(10, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .allMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));

        testUsers.forEach((username, id) -> {
            assertEquals(0, getAssignedUserClientRoleMapping(id).size());
        });
    }

}
