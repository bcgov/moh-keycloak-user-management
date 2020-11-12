package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
public class ClientController {

    private final WebClientService webClientService;
    private final Logger logger = LoggerFactory.getLogger(ClientController.class);

    public ClientController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/clients")
    public Mono<Object> clients(@RequestHeader("Authorization") String token) {

        JSONArray roles = parseRoles(token);
        List<String> authorizedClients = parseClients(roles);

        return webClientService.getClients(authorizedClients);
    }

    private JSONArray parseRoles(String token) {

        JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        JSONArray roles = new JSONArray();
        String[] parts = token.split("\\.");

        try {
            JSONObject payload = (JSONObject) jsonParser.parse(decodeBase64(parts[1]));
            JSONObject resourceAccess = (JSONObject) payload.get("resource_access");
            JSONObject userManagementService = (JSONObject) resourceAccess.get("user-management-service");
            roles = (JSONArray) userManagementService.get("roles");
        } catch (ParseException ex) {
            logger.error(ex.getMessage());
        }

        return roles;
    }

    private List<String> parseClients(JSONArray roles) {

        List<String> clients = new ArrayList<>();

        roles.forEach(role -> {
            String[] parts = ((String) role).split("view-client-");
            if (parts.length == 2) {
                clients.add(parts[1].toLowerCase());
            }
        });

        return clients;
    }

    private static String decodeBase64(String stringToDecode) {

        byte[] bytesToDecode = stringToDecode.getBytes(StandardCharsets.UTF_8);
        byte[] decodedBytes = Base64.getDecoder().decode(bytesToDecode);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

        return decodedString;
    }
}
