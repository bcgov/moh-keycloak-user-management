package ca.bc.gov.hlth.mohums.loadtest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.CollectionUtils;

/**
 * This class attempts to create as many users as indicated under the LOADTEST client, assigning
 * them to its single client role. This is needed to verify that Search By Role in the UMS front end
 * can handle a large volume of users assigned to a single role, particularly when the search is
 * combined with other criteria (name, email, etc.)
 */
class LoadTestUserProducer {

    static final Logger LOGGER = LoggerFactory.getLogger("LOAD_TEST");

    private static final String CLIENT_NAME = "LOADTEST";

    private static final String BASE_USERNAME = "loadtest";

    private static final Pattern USERNAME_PATTERN = Pattern.compile(BASE_USERNAME + "(\\d+)");

    private final WebTestClient webTestClient;

    private final String jwt;

    private final String testClientId;

    private final String testRoleId;

    private final String testRoleName;

    /**
     * @param webTestClient {@code @Autowired} web test client.
     * @param jwt valid Keycloak authorization token.
     * @throws Exception if unable to retrieve the client information or its single client role
     * details.
     */
    LoadTestUserProducer(WebTestClient webTestClient, String jwt) throws Exception {
        this.webTestClient = webTestClient;
        this.jwt = jwt;

        Map<String, ?> client = getClient(CLIENT_NAME);
        this.testClientId = Objects.toString(client.get("id"));

        Map<String, ?> clientRole = getClientRole(this.testClientId);
        this.testRoleId = Objects.toString(clientRole.get("id"));
        this.testRoleName = Objects.toString(clientRole.get("name"));
    }

    private List<Object> getAll(String resource) {
        LOGGER.debug("Retrieving all resources under /{}", resource);

        return webTestClient
                .get()
                .uri("/" + resource)
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();
    }

    private Map<String, ?> getClient(String clientName) {
        Optional<Object> retrievedClient = getAll("clients")
                .stream()
                .filter(c -> clientName.equalsIgnoreCase(((Map<String, String>) c).get("name")))
                .findFirst();

        Assertions.assertThat(retrievedClient).isPresent();

        LOGGER.debug("Retrieved client: {}", retrievedClient.get());

        return (Map<String, ?>) retrievedClient.get();
    }

    private Map<String, ?> getClientRole(String clientId) throws Exception {
        LOGGER.debug("Retrieving client roles for {}", clientId);

        List<Object> clientRoles = webTestClient
                .get()
                .uri("/clients/" + clientId + "/roles")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(clientRoles).hasSize(1).first()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("clientRole", Boolean.TRUE);

        LOGGER.debug("Retrieved client roles: {}", clientRoles);

        return (Map<String, ?>) clientRoles.get(0);
    }

    List<Map<String, ?>> getAllUsersInRole() throws Exception {
        LOGGER.debug("Retrieving all users in role {}", this.testRoleName);

        // This seems to be the default 'max' value used by Keycloak when querying a resource.
        // Increase it if you want to have a smaller number of queries against
        // /clients/.../roles/.../users.
        int max = 100;
        AtomicInteger first = new AtomicInteger(0);

        List<Map<String, ?>> allUsers = new LinkedList<>();
        int retrievedUsers;

        do {
            List<Object> usersInRole = webTestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/clients/"
                            + this.testClientId
                            + "/roles/"
                            + this.testRoleName
                            + "/users")
                    .queryParam("briefRepresentation", true)
                    .queryParam("first", first.get())
                    .queryParam("max", max)
                    .build())
                    .header("Authorization", "Bearer " + jwt)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(Object.class)
                    .returnResult()
                    .getResponseBody();

            if (CollectionUtils.isEmpty(usersInRole)) {
                retrievedUsers = 0;

                LOGGER.debug("Unable to retrieve more users with role {}, starting with first = {}",
                        this.testRoleName, first.get());
            } else {
                retrievedUsers = usersInRole.size();

                LOGGER.debug("Retrieved {} users with role {}, starting with first = {}",
                        retrievedUsers, this.testRoleName, first.get());

                first.addAndGet(retrievedUsers);

                usersInRole.stream().map(user -> (Map<String, ?>) user)
                        .forEach(allUsers::add);
            }
        } while (retrievedUsers > 0);

        return allUsers;
    }

    void createTestUsers(int desiredUserCount) throws Exception {
        List<Map<String, ?>> existingUsers = getAllUsersInRole();

        OptionalInt maxSequence = existingUsers.stream()
                .map(user -> user.get("username").toString())
                .map(USERNAME_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .mapToInt(Integer::valueOf)
                .max();

        int nextSequence = maxSequence.orElse(0) + 1;

        LOGGER.debug("Next sequence: {}", nextSequence);

        for (int counter = existingUsers.size();
                counter < desiredUserCount;
                counter++, nextSequence++) {
            String userId = createTestUser(nextSequence);

            LOGGER.debug("Created user with ID {} for test user with sequence {}", userId,
                    nextSequence);

            addClientRoleToUser(userId);
        }
    }

    private String createTestUser(int nextSequence) throws Exception {
        String sequence = String.format("%04d", nextSequence);
        String username = BASE_USERNAME + sequence;

        LOGGER.debug("Creating test user {}", username);

        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"enabled\":true,\"username\":\""
                        + username
                        + "\",\"firstName\":\"Test-"
                        + sequence
                        + "\",\"lastName\":\"Load\",\"email\":\""
                        + username
                        + "@load.test\",\"emailVerified\":\"\",\"attributes\":{}}")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().value(value -> Assertions.assertThat(value)
                .isIn(HttpStatus.CREATED.value(), HttpStatus.CONFLICT.value()));

        final List<Object> createdUsers = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                .path("/users")
                .queryParam("username", username)
                .queryParam("briefRepresentation", true)
                .build())
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(createdUsers).hasSize(1)
                .first()
                .extracting("username").asString().isEqualToIgnoringCase(username);

        return createdUsers.stream()
                .map(createdUser -> (Map<String, String>) createdUser)
                .map(user -> user.get("id"))
                .findFirst()
                .get();
    }

    private void addClientRoleToUser(String userId) throws Exception {
        LOGGER.debug("Adding role {} to test user with ID {}", this.testRoleName, userId);

        webTestClient
                .post()
                .uri("/users/"
                        + userId
                        + "/role-mappings/clients/"
                        + this.testClientId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("[\n"
                        + "    {\n"
                        + "        \"id\": \"" + this.testRoleId + "\",\n"
                        + "        \"name\": \"" + this.testRoleName + "\",\n"
                        + "        \"composite\": false,\n"
                        + "        \"clientRole\": true,\n"
                        + "        \"containerId\": \"" + this.testClientId + "\"\n"
                        + "    }\n"
                        + "]")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isNoContent();
    }

}
