package ca.bc.gov.hlth.mohums.loadtest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(Lifecycle.PER_CLASS)
public class MoHUmsPrepareLoadTests {

    private static final JSONParser JSON_PARSER = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    String keycloakTokenUri;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    String clientSecret;

    @Autowired
    private WebTestClient webTestClient;

    private String jwt;

    private LoadTestUserProducer loadTestUserProducer;

    @BeforeAll
    public void createTestUsers() throws Exception {
        jwt = getKcAccessToken();

        loadTestUserProducer = new LoadTestUserProducer(webTestClient, jwt);
    }

    private String getKcAccessToken() throws IOException, InterruptedException, ParseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakTokenUri))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("cache-control", "no-cache")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBodyAsJson = (JSONObject) JSON_PARSER.parse(response.body());
        String access_token = responseBodyAsJson.get("access_token").toString();

        return access_token;
    }

    @Test
    public void createDesiredUserCount() throws Exception {
        final int desiredUserCount = 1100;

        loadTestUserProducer.createTestUsers(desiredUserCount);

        List<String> loadTestUserNames = loadTestUserProducer.getAllUsersInRole()
                .stream()
                .map(user -> user.get("username").toString())
                .sorted()
                .collect(Collectors.toList());

        LoadTestUserProducer.LOGGER.info("Test users: {}", loadTestUserNames);

        Assertions.assertThat(loadTestUserNames).hasSizeGreaterThanOrEqualTo(desiredUserCount);
    }

}
