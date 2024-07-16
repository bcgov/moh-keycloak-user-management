package ca.bc.gov.hlth.mohums.integration;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Configuration
public class IntegrationTestsUtils {
    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    @Value("${spring.security.oauth2.client.provider.keycloak-moh.token-uri}")
    String keycloakMohTokenUri;

    @Value("${spring.security.oauth2.client.provider.keycloak-master.token-uri}")
    String keycloakMasterTokenUri;


    public String getMohApplicationsKcAccessToken(String clientId, String clientSecret) throws IOException, InterruptedException, ParseException {
        return getAccessToken(keycloakMohTokenUri, clientId, clientSecret);
    }

    public String getMasterRealmKcToken(String clientId, String clientSecret) throws IOException, InterruptedException, ParseException {
        return getAccessToken(keycloakMasterTokenUri, clientId, clientSecret);

    }

    private String getAccessToken(String tokenUri, String clientId, String clientSecret) throws IOException, InterruptedException, ParseException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri))
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
}
