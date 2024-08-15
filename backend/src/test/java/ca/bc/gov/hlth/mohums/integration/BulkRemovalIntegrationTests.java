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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @BeforeAll
    public void getJWT() throws InterruptedException, ParseException, IOException {
        jwt = integrationTestsUtils.getMohApplicationsKcAccessToken(clientId, clientSecret);

        webTestClient = webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(120))
                .build();

    }

    /**
     * Constants used for integration tests:
     * UMS_INTEGRATION_TESTS_CLIENT_ID -> ID of a Keycloak DEV client (service account) who is calling the bulk-removal endpoint.
     * BULK_REMOVAL_USER_UMS_1 and 2 -> ID of a Keycloak DEV user whose roles are assigned and revoked.
     * NON_EXISTING - constant for unhappy path testing. Used to mimic invalid user or role ID.
     */

    private static final String UMS_INTEGRATION_TESTS_CLIENT_ID = "24447cb4-f3b1-455b-89d9-26c081025fb9";
    private static final String BULK_REMOVAL_USER_UMS_1 = "3d78de77-86dc-41a3-a3d9-432a494d9147";
    private static final String BULK_REMOVAL_USER_UMS_2 = "ead9626f-df90-4c95-91d4-e4e447afde5f";
    private static final String BULK_REMOVAL_USER_UMS_3 = "c5b18aed-b2c7-4a60-9ead-379f2bfa795b";
    private static final String BULK_REMOVAL_USER_UMS_4 = "f310236b-e294-4746-8536-092a946fef39";
    private static final String BULK_REMOVAL_USER_UMS_5 = "eea5202b-d9d0-4efd-8b2d-bc0a76a6a8d5";
    private static final String BULK_REMOVAL_USER_UMS_6 = "9f306a79-2790-4fbc-8a08-e61476fc7ea4";
    private static final String BULK_REMOVAL_USER_UMS_7 = "57600b08-022f-4781-b88d-a6a756c73c5c";
    private static final String BULK_REMOVAL_USER_UMS_8 = "97b9a0ec-58ce-4318-b5f6-9b30bfb41e2c";
    private static final String BULK_REMOVAL_USER_UMS_9 = "ba9ab966-a9a9-4447-904f-60c26a0ac9da";
    private static final String BULK_REMOVAL_USER_UMS_10 = "6f56aae7-de9c-4a66-92d8-22288f9f11d6";
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
                Arguments.of(new BulkRemovalRequest(Map.of(BULK_REMOVAL_USER_UMS_1, Collections.emptyList())), "List of roles to remove cannot be null or empty")
        );
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

    @Test
    @SuppressWarnings("unchecked")
    public void bulkRemoveTwoRolesTenUsersSuccess() {
        addTestRoles(BULK_REMOVAL_USER_UMS_1);
        addTestRoles(BULK_REMOVAL_USER_UMS_2);
        addTestRoles(BULK_REMOVAL_USER_UMS_3);
        addTestRoles(BULK_REMOVAL_USER_UMS_4);
        addTestRoles(BULK_REMOVAL_USER_UMS_5);
        addTestRoles(BULK_REMOVAL_USER_UMS_6);
        addTestRoles(BULK_REMOVAL_USER_UMS_7);
        addTestRoles(BULK_REMOVAL_USER_UMS_8);
        addTestRoles(BULK_REMOVAL_USER_UMS_9);
        addTestRoles(BULK_REMOVAL_USER_UMS_10);

        BulkRemovalRequest bulkRemovalRequest = new BulkRemovalRequest(
                Map.of(BULK_REMOVAL_USER_UMS_1, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_2, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_3, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_4, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_5, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_6, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_7, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_8, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_9, List.of(getBulkRemovalRole1(), getBulkRemovalRole2()),
                        BULK_REMOVAL_USER_UMS_10, List.of(getBulkRemovalRole1(), getBulkRemovalRole2())));

        List<Object> response = bulkRemove(bulkRemovalRequest, UMS_INTEGRATION_TESTS_CLIENT_ID);

        assertEquals(10, response.size());
        assertTrue(response.stream()
                .map(responseItem -> (LinkedHashMap<String, Object>) responseItem)
                .allMatch(responseItem -> responseItem.get("statusCode").equals("NO_CONTENT")));

        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_1).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_2).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_3).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_4).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_5).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_6).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_7).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_8).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_9).size());
        assertEquals(0, getAssignedUserClientRoleMapping(BULK_REMOVAL_USER_UMS_10).size());
    }

}
