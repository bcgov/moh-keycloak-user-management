package ca.bc.gov.hlth.mohums.integration;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrgApiIntegrationTests {

    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    @Value("${spring.security.oauth2.client.provider.keycloak-moh.token-uri}")
    String keycloakTokenUri;

    @Value("${spring.security.oauth2.client.registration.keycloak-moh.client-id}")
    String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak-moh.client-secret}")
    String clientSecret;

    @Value("${keycloak-moh.organizations-api-url}")
    private String organizationsApiBaseUrl;

    @Autowired
    private WebTestClient orgApiWebTestClient;

    private String jwt;

    private String testOrgId = "99999999";
    private String testOrgName = "Integration test org";

    @BeforeAll
    public void getJWT() throws InterruptedException, ParseException, IOException {
        jwt = getKcAccessToken();

        orgApiWebTestClient = orgApiWebTestClient
                .mutate()
                .baseUrl(organizationsApiBaseUrl)
                .responseTimeout(Duration.ofSeconds(120))
                .build();

        addTestOrg();
    }

    @AfterAll
    public void cleanupOrgDatabase() {
        deleteTestOrg();
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
    public void getOrganizations() {
        List<Object> organizations = orgApiWebTestClient
                .get()
                .uri("/organizations")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(organizations).isNotEmpty();
    }

    @Test
    public void getOrganizationById() {
        Assumptions.assumeTrue(isDevEnvironment());

        Object organization = orgApiWebTestClient
                .get()
                .uri(String.format("/organizations/00001480"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Object.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(organization).isNotNull();
    }

    @Test
    public void getOrganizationByIdNotFound() {
        orgApiWebTestClient
                .get()
                .uri("/organizations/invalidOrganizationId")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void addOrganizationSuccess() {
        Assumptions.assumeTrue(isDevEnvironment());

        deleteTestOrg();

        orgApiWebTestClient
                .post()
                .uri("/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(String.format("{\"organizationId\":\"%s\",\"name\":\"%s\"}", testOrgId, testOrgName))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Object.class);
    }

    @Test
    public void addOrganizationFailure() {
        Assumptions.assumeTrue(isDevEnvironment());

        orgApiWebTestClient
                .post()
                .uri("/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"organizationId\":\"99999999\",\"name\":\"Hi Dad\"}")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    /* test ignored until further talks about editing organizations */
    @Test
    @Ignore
    public void editOrganizationSuccess() {
        Assumptions.assumeTrue(isDevEnvironment());

        String editedOrgName = "Edited during integration test";
        orgApiWebTestClient
                .put()
                .uri("/organizations/00001480")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(String.format("{\"organizationId\":\"%s\",\"name\":\"%s\"}", testOrgId, editedOrgName))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk();
    }

    private void addTestOrg() {
        orgApiWebTestClient
                .post()
                .uri("/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(String.format("{\"organizationId\":\"%s\",\"name\":\"%s\"}", testOrgId, testOrgName))
                .header("Authorization", "Bearer " + jwt)
                .exchange();
    }

    private void deleteTestOrg() {
        Object a = orgApiWebTestClient
                .delete()
                .uri(String.format("/organizations/%s", testOrgId))
                .header("Authorization", "Bearer " + jwt)
                .exchange();

        System.out.println(a);
    }

    private boolean isDevEnvironment() {
        return organizationsApiBaseUrl.contains("dev");
    }
}
