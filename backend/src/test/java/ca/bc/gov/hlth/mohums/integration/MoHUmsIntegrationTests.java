package ca.bc.gov.hlth.mohums.integration;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
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

    @Before
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
        webTestClient
                .get()
                .uri("/users")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk();
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
    public void clientsNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/clients")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }
}
